// Copyright 2018, Yahoo Holdings Inc.
// Licensed under the terms of the Apache License 2.0. Please see LICENSE file in project root for terms.
package com.yahoo.maha.service.curators

import com.yahoo.maha.core.request._
import com.yahoo.maha.service.BaseMahaServiceTest
import com.yahoo.maha.service.example.ExampleSchema.StudentSchema
import org.scalatest.BeforeAndAfterAll

class DrilldownConfigTest extends BaseMahaServiceTest with BeforeAndAfterAll {

  override protected def afterAll(): Unit =  {
    super.afterAll()
    server.shutdownNow()
  }

  test("Create a valid DrillDownConfig") {
    val json : String =
      s"""{
                          "cube": "student_performance",
                          "curators" : {
                            "drilldown" : {
                              "config" : {
                                "enforceFilters": "true",
                                "dimension": "Section ID",
                                "ordering": [{
                                              "field": "Class ID",
                                              "order": "asc"
                                              }],
                                "mr": 1000
                              }
                            }
                          },
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "sortBy": [
                            {"field": "Total Marks", "order": "Desc"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "2018-01-01", "to": "2018-01-02"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""
    val reportingRequestResult = ReportingRequest.deserializeSyncWithFactBias(json.getBytes, schema = StudentSchema)
    require(reportingRequestResult.isSuccess)
    val reportingRequest = reportingRequestResult.toOption.get

    val drillDownConfig : DrilldownConfig = DrilldownConfig.parse(reportingRequest.curatorJsonConfigMap("drilldown")).toOption.get

    
    assert(!drillDownConfig.enforceFilters)
    assert(drillDownConfig.maxRows == 1000)
    assert(drillDownConfig.ordering.contains(SortBy("Class ID", ASC)))
    assert(drillDownConfig.dimension == Field("Section ID", None, None))
    assert(drillDownConfig.cube == "")
  }

  test("Create a valid DrillDownConfig with reversed ordering") {
    val json : String =
      s"""{
                          "cube": "student_performance",
                          "curators" : {
                            "drilldown" : {
                              "config" : {
                                "enforceFilters": "true",
                                "dimension": "Section ID",
                                "ordering": [{
                                              "field": "Class ID",
                                              "order": "desc"
                                              }],
                                "mr": 1000
                              }
                            }
                          },
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "sortBy": [
                            {"field": "Total Marks", "order": "Desc"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "2018-01-01", "to": "2018-01-02"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""
    val reportingRequestResult = ReportingRequest.deserializeSyncWithFactBias(json.getBytes, schema = StudentSchema)
    require(reportingRequestResult.isSuccess)
    val reportingRequest = reportingRequestResult.toOption.get

    val drillDownConfig : DrilldownConfig = DrilldownConfig.parse(reportingRequest.curatorJsonConfigMap("drilldown")).toOption.get

    
    assert(!drillDownConfig.enforceFilters)
    assert(drillDownConfig.maxRows == 1000)
    assert(drillDownConfig.ordering.contains(SortBy("Class ID", DESC)))
    assert(drillDownConfig.dimension == Field("Section ID", None, None))
    assert(drillDownConfig.cube == "")
  }

  test("Create a valid DrillDownConfig with invalid ordering") {
    val json : String =
      s"""{
                          "cube": "student_performance",
                          "curators" : {
                            "drilldown" : {
                              "config" : {
                                "enforceFilters": "true",
                                "dimension": "Section ID",
                                "ordering": [{
                                              "field": "Class ID",
                                              "order": "willfail"
                                              }],
                                "mr": 1000
                              }
                            }
                          },
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "sortBy": [
                            {"field": "Total Marks", "order": "Desc"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "2018-01-01", "to": "2018-01-02"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""
    val reportingRequestResult = ReportingRequest.deserializeSyncWithFactBias(json.getBytes, schema = StudentSchema)
    require(reportingRequestResult.isSuccess)
    val reportingRequest = reportingRequestResult.toOption.get

    val thrown = intercept[Exception] {
      DrilldownConfig.parse(reportingRequest.curatorJsonConfigMap("drilldown"))
    }
    assert(thrown.getMessage.contains("order must be asc|desc not willfail"))
  }

  test("DrillDownConfig should throw error on no dimension.") {
    val json : String =
      s"""{
                          "cube": "student_performance",
                          "curators" : {
                            "drilldown" : {
                              "config" : {
                                "enforceFilters": "true",
                                "ordering": [{
                                              "field": "Class ID",
                                              "order": "asc"
                                              }],
                                "mr": 1000
                              }
                            }
                          },
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "sortBy": [
                            {"field": "Total Marks", "order": "Desc"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "2018-01-01", "to": "2018-01-02"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""
    val reportingRequestResult = ReportingRequest.deserializeSyncWithFactBias(json.getBytes, schema = StudentSchema)
    require(reportingRequestResult.isSuccess)
    val reportingRequest = reportingRequestResult.toOption.get

    val thrown = intercept[Exception] {
      DrilldownConfig.parse(reportingRequest.curatorJsonConfigMap("drilldown"))
    }
    assert(thrown.getMessage.contains("CuratorConfig for a DrillDown should have a dimension declared"))
  }

  test("Create a valid DrillDownConfig with Descending order and multiple orderings") {
    val json : String =
      s"""{
                          "cube": "student_performance",
                          "curators" : {
                            "drilldown" : {
                              "config" : {
                                "dimension": "Section ID",
                                "ordering": [{
                                              "field": "Class ID",
                                              "order": "asc"
                                              },
                                              {
                                               "field": "Section ID",
                                               "order": "Desc"
                                              }]
                              }
                            }
                          },
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "sortBy": [
                            {"field": "Total Marks", "order": "Desc"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "2018-01-01", "to": "2018-01-02"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""
    val reportingRequestResult = ReportingRequest.deserializeSyncWithFactBias(json.getBytes, schema = StudentSchema)
    require(reportingRequestResult.isSuccess)
    val reportingRequest = reportingRequestResult.toOption.get

    val drillDownConfig : DrilldownConfig = DrilldownConfig.parse(reportingRequest.curatorJsonConfigMap("drilldown")).toOption.get

    
    assert(drillDownConfig.enforceFilters == false)
    assert(drillDownConfig.maxRows == 1000)
    assert(drillDownConfig.ordering.contains(SortBy("Section ID", DESC)))
    assert(drillDownConfig.dimension == Field("Section ID", None, None))
    assert(drillDownConfig.cube == "")
  }

  test("Create a valid DrillDownConfig with Descending order and no given ordering") {
    val json : String =
      s"""{
                          "cube": "student_performance",
                          "curators" : {
                            "drilldown" : {
                              "config" : {
                                "enforceFilters": true,
                                "dimension": "Day",
                                "mr": 1000
                              }
                            }
                          },
                          "selectFields": [
                            {"field": "Student ID"},
                            {"field": "Class ID"},
                            {"field": "Section ID"},
                            {"field": "Total Marks"}
                          ],
                          "sortBy": [
                            {"field": "Total Marks", "order": "Desc"}
                          ],
                          "filterExpressions": [
                            {"field": "Day", "operator": "between", "from": "2018-01-01", "to": "2018-01-02"},
                            {"field": "Student ID", "operator": "=", "value": "213"}
                          ]
                        }"""
    val reportingRequestResult = ReportingRequest.deserializeSyncWithFactBias(json.getBytes, schema = StudentSchema)
    require(reportingRequestResult.isSuccess)
    val reportingRequest = reportingRequestResult.toOption.get

    val drillDownConfig : DrilldownConfig = DrilldownConfig.parse(reportingRequest.curatorJsonConfigMap("drilldown")).toOption.get

    
    assert(drillDownConfig.enforceFilters)
    assert(drillDownConfig.maxRows == 1000)
    assert(drillDownConfig.dimension == Field("Day", None, None))
    assert(drillDownConfig.cube == "")
  }
}
