package com.softserve.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@DisplayName("Semesters API Tests")
class SemesterApiTest {

    private static final String BASE_URI = "http://localhost:8080";
    private static final String MANAGER_EMAIL = "manager@gmail.com";
    private static final String MANAGER_PASSWORD = "Qwerty!123";

    private static final int FALLBACK_PERIOD_ID = 4;
    private static final int NEW_SEMESTER_YEAR = 2037;

    private static String token;
    private static int seedSemesterId;
    private static String seedSemesterDescription;
    private static int seedSemesterYear;
    private static int periodIdForJson = FALLBACK_PERIOD_ID;

    private static Integer createdSemesterId;
    private static String createdDescription;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
        token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s","password":"%s"}
                        """.formatted(MANAGER_EMAIL, MANAGER_PASSWORD))
                .when()
                .post("/auth/sign-in")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
        assertNotNull(token);

        List<Map<String, Object>> semesters = auth()
                .when()
                .get("/semesters")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");
        Assumptions.assumeFalse(semesters == null || semesters.isEmpty(),
                "Потрібен хоча б один семестр у БД сервера для цих тестів");

        Map<String, Object> first = semesters.get(0);
        seedSemesterId = ((Number) first.get("id")).intValue();
        seedSemesterDescription = (String) first.get("description");
        seedSemesterYear = ((Number) first.get("year")).intValue();

        List<Map<String, Object>> classes = auth()
                .when()
                .get("/semesters/{id}", seedSemesterId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("semester_classes");
        if (classes != null && !classes.isEmpty() && classes.get(0).get("id") != null) {
            periodIdForJson = ((Number) classes.get(0).get("id")).intValue();
        }
    }

    private static RequestSpecification auth() {
        return given()
                .header("Authorization", "Bearer_" + token)
                .contentType(ContentType.JSON);
    }

    private static String newSemesterJson(String description, int year) {
        return """
                {
                  "description": "%s",
                  "year": %d,
                  "startDay": "20/01/%d",
                  "endDay": "20/06/%d",
                  "currentSemester": false,
                  "defaultSemester": false,
                  "disable": false,
                  "semester_days": ["MONDAY", "FRIDAY"],
                  "semester_classes": [
                    {
                      "id": %d,
                      "class_name": "first para",
                      "startTime": "09:00",
                      "endTime": "10:00"
                    }
                  ]
                }
                """.formatted(description, year, year, year, periodIdForJson);
    }
    
    @Nested
    @DisplayName("Semesters API")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Flow {

        @Test
        @Order(1)
        @DisplayName("GET /semesters — should return list")
        void getAll_returnsList() {
            auth()
                    .when()
                    .get("/semesters")
                    .then()
                    .statusCode(200)
                    .body("$", instanceOf(List.class));
        }

        @Test
        @Order(2)
        @DisplayName("GET /semesters — should return required fields on first item")
        void getAll_firstItemHasFields() {
            auth()
                    .when()
                    .get("/semesters")
                    .then()
                    .statusCode(200)
                    .body("[0].id", notNullValue())
                    .body("[0].description", notNullValue());
        }

        @Test
        @Order(3)
        @DisplayName("GET /semesters/{id} — should return seed semester")
        void getById_returns200() {
            auth()
                    .when()
                    .get("/semesters/{id}", seedSemesterId)
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("description", equalTo(seedSemesterDescription));
        }

        @Test
        @Order(4)
        @DisplayName("GET /semesters/{id} — should return 404")
        void getById_returns404() {
            auth()
                    .when()
                    .get("/semesters/{id}", 999_999_999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        @Order(5)
        @DisplayName("POST /semesters — should create semester")
        void postCreate_returns201() {
            createdDescription = "ApiSem_" + System.currentTimeMillis();
            createdSemesterId = auth()
                    .body(newSemesterJson(createdDescription, NEW_SEMESTER_YEAR))
                    .when()
                    .post("/semesters")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("description", equalTo(createdDescription))
                    .extract()
                    .path("id");
        }

        @Test
        @Order(6)
        @DisplayName("POST /semesters — should return 400 for duplicate description and year")
        void postDuplicate_returns400() {
            auth()
                    .body(newSemesterJson(seedSemesterDescription, seedSemesterYear))
                    .when()
                    .post("/semesters")
                    .then()
                    .statusCode(400);
        }

        @Test
        @Order(7)
        @DisplayName("PUT /semesters — should update semester")
        void put_returns200() {
            String updatedDescription = "ApiSemUpd_" + System.currentTimeMillis();
            auth()
                    .body("""
                            {
                              "id": %d,
                              "description": "%s",
                              "year": 2022,
                              "startDay": "10/02/2022",
                              "endDay": "10/08/2022",
                              "currentSemester": false,
                              "defaultSemester": false,
                              "disable": false,
                              "semester_days": ["MONDAY", "FRIDAY"],
                              "semester_classes": [
                                {
                                  "id": %d,
                                  "class_name": "first para",
                                  "startTime": "09:00",
                                  "endTime": "10:00"
                                }
                              ]
                            }
                            """.formatted(createdSemesterId, updatedDescription, periodIdForJson))
                    .when()
                    .put("/semesters")
                    .then()
                    .statusCode(200)
                    .body("description", equalTo(updatedDescription));
        }

        @Test
        @Order(8)
        @DisplayName("DELETE /semesters/{id} — should return 200")
        void delete_returns200() {
            auth()
                    .when()
                    .delete("/semesters/{id}", createdSemesterId)
                    .then()
                    .statusCode(200);
        }

        @Test
        @Order(9)
        @DisplayName("GET /semesters/{id} — should return 404 after delete")
        void getById_afterDelete_returns404() {
            auth()
                    .when()
                    .get("/semesters/{id}", createdSemesterId)
                    .then()
                    .statusCode(404);
        }
    }
}
