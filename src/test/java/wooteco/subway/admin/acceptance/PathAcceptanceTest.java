package wooteco.subway.admin.acceptance;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import wooteco.subway.admin.dto.LineResponse;
import wooteco.subway.admin.dto.PathResponse;
import wooteco.subway.admin.dto.StationResponse;

public class PathAcceptanceTest extends AcceptanceTest {
    private StationResponse jamsil;
    private StationResponse jamsilsaenae;
    private StationResponse playgound;
    private StationResponse samjeon;
    private StationResponse seokchongobun;
    private StationResponse seokchon;
    private StationResponse songnae;

    private LineResponse line2;
    private LineResponse line3;
    private LineResponse line4;
    private LineResponse lineBunDang;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();

        jamsil = createStation(STATION_NAME_JAMSIL);
        jamsilsaenae = createStation(STATION_NAME_JAMSILSAENAE);
        playgound = createStation(STATION_NAME_PLAYGROUND);
        samjeon = createStation(STATION_NAME_SAMJEON);
        seokchongobun = createStation(STATION_NAME_SEOKCHOENGOBUN);
        seokchon = createStation(STATION_NAME_SEOCKCHEON);
        songnae = createStation(STATION_NAME_SONGNAE);

        line2 = createLine(LINE_NAME_2);
        line3 = createLine(LINE_NAME_3);
        line4 = createLine(LINE_NAME_4);
        lineBunDang = createLine(LINE_NAME_BUNDANG);

        addLineStation(line2.getId(), null, jamsil.getId(), 0, 0);
        addLineStation(line2.getId(), jamsil.getId(), jamsilsaenae.getId(), 10, 1);
        addLineStation(line2.getId(), jamsilsaenae.getId(), playgound.getId(), 10, 1);

        addLineStation(lineBunDang.getId(), null, playgound.getId(), 0, 0);
        addLineStation(lineBunDang.getId(), playgound.getId(), samjeon.getId(), 10, 1);
        addLineStation(lineBunDang.getId(), samjeon.getId(), seokchongobun.getId(), 1, 10);
        addLineStation(lineBunDang.getId(), seokchongobun.getId(), seokchon.getId(), 1, 10);

        addLineStation(line3.getId(), null, jamsil.getId(), 0, 0);
        addLineStation(line3.getId(), jamsil.getId(), seokchon.getId(), 1, 10);

        addLineStation(line4.getId(), null, songnae.getId(), 10, 10);
    }

    @DisplayName("최단경로를 조회한다")
    @Test
    void findPathByDistance() {
        //when
        PathResponse path = getPath(jamsil.getName(), samjeon.getName(), "distance");

        //then
        assertThat(path.getStations()).hasSize(4);
        assertThat(path.getStations()).extracting(StationResponse::getName)
            .containsExactly(STATION_NAME_JAMSIL, STATION_NAME_SEOCKCHEON, STATION_NAME_SEOKCHOENGOBUN,
                STATION_NAME_SAMJEON);
        assertThat(path.getDistance()).isEqualTo(3);
        assertThat(path.getDuration()).isEqualTo(30);
    }

    @DisplayName("최단경로를 이어있지 않은 역을 조회해 실패한다")
    @Test
    void NotfoundPathByDistanceWhenNotConnection() {
        notFoundPath(jamsil.getName(), songnae.getName(), "distance");
    }

    @DisplayName("최단경로를 같은역을 조회해 실패한다")
    @Test
    void NotfoundPathByDistanceWhenSameSourceAndTarget() {
        notFoundPath(jamsil.getName(), jamsil.getName(), "distance");
    }

    @DisplayName("최단경로를 존재하지 않는 역을 조회해 실패한다")
    @Test
    void NotfoundPathByDistanceWhenNotExistStation() {
        notFoundPath(jamsil.getName(), "부개", "distance");
    }

    @DisplayName("경로를 최단거리와 최소시간으로 조회단다")
    @Test
    void findPathByDistanceAndDuration() {
        //when
        PathResponse pathByDistance = getPath(jamsil.getName(), seokchongobun.getName(), "distance");

        //then
        assertThat(pathByDistance.getStations()).hasSize(3);
        assertThat(pathByDistance.getStations()).extracting(StationResponse::getName)
            .containsExactly(STATION_NAME_JAMSIL, STATION_NAME_SEOCKCHEON, STATION_NAME_SEOKCHOENGOBUN);
        assertThat(pathByDistance.getDistance()).isEqualTo(2);
        assertThat(pathByDistance.getDuration()).isEqualTo(20);

        //when
        PathResponse pathByDuration = getPath(jamsil.getName(), seokchongobun.getName(), "duration");

        //then
        assertThat(pathByDuration.getStations()).hasSize(5);
        assertThat(pathByDuration.getStations()).extracting(StationResponse::getName)
            .containsExactly(STATION_NAME_JAMSIL, STATION_NAME_JAMSILSAENAE, STATION_NAME_PLAYGROUND,
                STATION_NAME_SAMJEON, STATION_NAME_SEOKCHOENGOBUN);
        assertThat(pathByDuration.getDistance()).isEqualTo(31);
        assertThat(pathByDuration.getDuration()).isEqualTo(13);
    }

    private PathResponse getPath(String source, String target, String criteriaType) {
        Map<String, String> params = getParams(source, target, criteriaType);

        return given().
            body(params).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            accept(MediaType.APPLICATION_JSON_VALUE).
            when().
            post("/paths").
            then().
            log().all().
            statusCode(HttpStatus.OK.value()).
            extract().as(PathResponse.class);
    }

    private void notFoundPath(String source, String target, String criteriaType) {
        Map<String, String> params = getParams(source, target, criteriaType);

        given().
            body(params).
            contentType(MediaType.APPLICATION_JSON_VALUE).
            accept(MediaType.APPLICATION_JSON_VALUE).
            when().
            post("/paths").
            then().
            log().all().
            statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    private Map<String, String> getParams(String source, String target, String criteriaType) {
        Map<String, String> params = new HashMap<>();
        params.put("source", source);
        params.put("target", target);
        params.put("criteria", criteriaType);
        return params;
    }

}