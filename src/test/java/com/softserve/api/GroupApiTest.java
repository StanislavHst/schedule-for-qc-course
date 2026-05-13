package com.softserve.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@DisplayName("Groups API Tests")
class GroupApiTest {

    private static final String BASE_URI = "http://localhost:8080";
    private static final String MANAGER_EMAIL = "manager@gmail.com";
    private static final String MANAGER_PASSWORD = "Qwerty!123";

    private static String token;
    private static Long anchorGroupId;
    private static Long teacherId;

    private static Integer createdGroupId;
    private static String createdGroupTitle;
    private static Integer afterOrderedGroupId;
    private static String afterOrderedTitle;

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

        Response groupsRes = auth()
                .when()
                .get("/groups")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Map<String, Object>> groups = groupsRes.jsonPath().getList("$");
        if (groups != null && !groups.isEmpty() && groups.get(0).get("id") != null) {
            anchorGroupId = ((Number) groups.get(0).get("id")).longValue();
        }

        Response teachersRes = auth()
                .when()
                .get("/teachers")
                .then()
                .statusCode(200)
                .extract()
                .response();
        List<Map<String, Object>> teachers = teachersRes.jsonPath().getList("$");
        if (teachers != null && !teachers.isEmpty() && teachers.get(0).get("id") != null) {
            teacherId = ((Number) teachers.get(0).get("id")).longValue();
        }
    }

    private static RequestSpecification auth() {
        return given()
                .header("Authorization", "Bearer_" + token)
                .contentType(ContentType.JSON);
    }


    @Nested
    @DisplayName("Groups API")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Flow {

        @Test
        @Order(1)
        @DisplayName("GET /groups — should return 200 and a list")
        void getAll_returns200AndList() {
            auth()
                    .when()
                    .get("/groups")
                    .then()
                    .statusCode(200)
                    .body("$", instanceOf(List.class));
        }

        @Test
        @Order(2)
        @DisplayName("GET /groups — first item should have id and title when list non-empty")
        void getAll_firstItemHasIdAndTitle() {
            List<Map<String, Object>> list = auth()
                    .when()
                    .get("/groups")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList("$");
            Assumptions.assumeFalse(list == null || list.isEmpty());
            auth()
                    .when()
                    .get("/groups")
                    .then()
                    .statusCode(200)
                    .body("[0].id", notNullValue())
                    .body("[0].title", notNullValue());
        }

        @Test
        @Order(3)
        @DisplayName("GET /groups/disabled — should return 200")
        void getDisabled_returns200() {
            auth()
                    .when()
                    .get("/groups/disabled")
                    .then()
                    .statusCode(200);
        }

        @Test
        @Order(4)
        @DisplayName("GET /groups/disabled — should return a JSON array")
        void getDisabled_returnsJsonArray() {
            auth()
                    .when()
                    .get("/groups/disabled")
                    .then()
                    .statusCode(200)
                    .body("$", instanceOf(List.class));
        }

        @Test
        @Order(5)
        @DisplayName("GET /groups/teacher/{teacherId} — should return 200 for existing teacher")
        void getByTeacherId_existing_returns200() {
            Assumptions.assumeTrue(teacherId != null, "Потрібен хоча б один викладач у БД");
            auth()
                    .when()
                    .get("/groups/teacher/{teacherId}", teacherId)
                    .then()
                    .statusCode(200)
                    .body("$", instanceOf(List.class));
        }

        @Test
        @Order(6)
        @DisplayName("GET /groups/teacher/{teacherId} — should return 200 for unknown id (empty list)")
        void getByTeacherId_unknown_returns200() {
            auth()
                    .when()
                    .get("/groups/teacher/{teacherId}", 9_999_999_999L)
                    .then()
                    .statusCode(200)
                    .body("$", instanceOf(List.class));
        }

        @Test
        @Order(7)
        @DisplayName("GET /groups/{id} — should return 200 for existing group")
        void getById_existing_returns200() {
            Assumptions.assumeTrue(anchorGroupId != null, "Потрібна хоча б одна група у БД");
            auth()
                    .when()
                    .get("/groups/{id}", anchorGroupId)
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("title", notNullValue());
        }

        @Test
        @Order(8)
        @DisplayName("GET /groups/{id} — should return 404 for unknown id")
        void getById_unknown_returns404() {
            auth()
                    .when()
                    .get("/groups/{id}", 999_999_999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        @Order(9)
        @DisplayName("GET /groups/{id}/with-students — should return 200")
        void getWithStudents_existing_returns200() {
            Assumptions.assumeTrue(anchorGroupId != null, "Потрібна хоча б одна група у БД");
            auth()
                    .when()
                    .get("/groups/{id}/with-students", anchorGroupId)
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("title", notNullValue());
        }

        @Test
        @Order(10)
        @DisplayName("GET /groups/{id}/with-students — should return 404 for unknown id")
        void getWithStudents_unknown_returns404() {
            auth()
                    .when()
                    .get("/groups/{id}/with-students", 999_999_999L)
                    .then()
                    .statusCode(404);
        }

        @Test
        @Order(11)
        @DisplayName("POST /groups — should return 201 and body")
        void postCreate_returns201() {
            createdGroupTitle = "ApiGrp_" + System.currentTimeMillis();
            createdGroupId = auth()
                    .body("""
                            {"title":"%s","disable":false}
                            """.formatted(createdGroupTitle))
                    .when()
                    .post("/groups")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("title", equalTo(createdGroupTitle))
                    .extract()
                    .path("id");
        }

        @Test
        @Order(12)
        @DisplayName("POST /groups — should return 400 for invalid title")
        void postCreate_invalid_returns400() {
            auth()
                    .body("{\"title\":\"\"}")
                    .when()
                    .post("/groups")
                    .then()
                    .statusCode(400);
        }

        @Test
        @Order(13)
        @DisplayName("POST /groups/after — should return 201 when afterId points to existing group")
        void postAfter_returns201() {
            Assumptions.assumeTrue(anchorGroupId != null, "Потрібна anchor-група для afterId");
            afterOrderedTitle = "ApiGrpAfter_" + System.currentTimeMillis();
            afterOrderedGroupId = auth()
                    .body("""
                            {"title":"%s","disable":false,"afterId":%d}
                            """.formatted(afterOrderedTitle, anchorGroupId))
                    .when()
                    .post("/groups/after")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("title", equalTo(afterOrderedTitle))
                    .extract()
                    .path("id");
        }

        @Test
        @Order(14)
        @DisplayName("POST /groups/after — should return 4xx for invalid afterId")
        void postAfter_invalidAfterId_returns4xx() {
            auth()
                    .body("""
                            {"title":"ApiGrpBadAfter_%d","disable":false,"afterId":999999999}
                            """.formatted(System.currentTimeMillis()))
                    .when()
                    .post("/groups/after")
                    .then()
                    .statusCode(anyOf(equalTo(400), equalTo(404), equalTo(422)));
        }

        @Test
        @Order(15)
        @DisplayName("GET /groups/{id} — should return created group by id")
        void getById_created_returns200() {
            auth()
                    .when()
                    .get("/groups/{id}", createdGroupId)
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue())
                    .body("title", equalTo(createdGroupTitle));
        }

        @Test
        @Order(16)
        @DisplayName("PUT /groups — should return 200 and updated title")
        void putUpdate_returns200() {
            String updatedTitle = "ApiGrpUpd_" + System.currentTimeMillis();
            auth()
                    .body("""
                            {"id":%d,"title":"%s","disable":false}
                            """.formatted(createdGroupId, updatedTitle))
                    .when()
                    .put("/groups")
                    .then()
                    .statusCode(200)
                    .body("title", equalTo(updatedTitle));
            createdGroupTitle = updatedTitle;
        }

        @Test
        @Order(17)
        @DisplayName("PUT /groups — should return 400 for empty title")
        void putUpdate_invalid_returns400() {
            auth()
                    .body("""
                            {"id":%d,"title":"","disable":false}
                            """.formatted(createdGroupId))
                    .when()
                    .put("/groups")
                    .then()
                    .statusCode(400);
        }

        @Test
        @Order(18)
        @DisplayName("PUT /groups/after — should return 200 when reordering group")
        void putAfter_returns200() {
            Assumptions.assumeTrue(anchorGroupId != null, "Потрібна anchor-група для afterId");
            auth()
                    .body("""
                            {"id":%d,"title":"%s","disable":false,"afterId":%d}
                            """.formatted(createdGroupId, createdGroupTitle, anchorGroupId))
                    .when()
                    .put("/groups/after")
                    .then()
                    .statusCode(200)
                    .body("id", notNullValue());
        }

        @Test
        @Order(19)
        @DisplayName("PUT /groups/after — should return 4xx for invalid afterId")
        void putAfter_invalidAfterId_returns4xx() {
            auth()
                    .body("""
                            {"id":%d,"title":"%s","disable":false,"afterId":999999999}
                            """.formatted(createdGroupId, createdGroupTitle))
                    .when()
                    .put("/groups/after")
                    .then()
                    .statusCode(anyOf(equalTo(400), equalTo(404), equalTo(422)));
        }

        @Test
        @Order(20)
        @DisplayName("DELETE /groups/{id} — should delete group created via POST /groups/after")
        void delete_afterOrdered_returns200() {
            Assumptions.assumeTrue(afterOrderedGroupId != null, "Попередній тест POST /groups/after мав створити групу");
            auth()
                    .when()
                    .delete("/groups/{id}", afterOrderedGroupId)
                    .then()
                    .statusCode(200);
        }

        @Test
        @Order(21)
        @DisplayName("DELETE /groups/{id} — should delete main created group")
        void delete_created_returns200() {
            auth()
                    .when()
                    .delete("/groups/{id}", createdGroupId)
                    .then()
                    .statusCode(200);
        }

        @Test
        @Order(22)
        @DisplayName("GET /groups/{id} — should return 404 after delete")
        void getById_afterDelete_returns404() {
            auth()
                    .when()
                    .get("/groups/{id}", createdGroupId)
                    .then()
                    .statusCode(404);
        }

        @Test
        @Order(23)
        @DisplayName("DELETE /groups/{id} — should return 404 when deleting same id again")
        void delete_alreadyRemoved_returns404() {
            auth()
                    .when()
                    .delete("/groups/{id}", createdGroupId)
                    .then()
                    .statusCode(404);
        }
    }
}
