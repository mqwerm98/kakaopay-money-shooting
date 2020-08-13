package kakaopay.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import kakaopay.money.dto.CreateShootingDto;
import kakaopay.money.dto.ReceiveMoneyDto;
import kakaopay.money.dto.ShootingInfoDto;
import kakaopay.money.entity.Room;
import kakaopay.money.entity.Shooting;
import kakaopay.money.repository.RoomRepository;
import kakaopay.money.repository.ShootingRepository;
import kakaopay.money.service.TestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class GetShootingInfoApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private TestService testService;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private ModelMapper modelMapper;

    @Autowired private RoomRepository roomRepository;

    @Autowired private ShootingRepository shootingRepository;

    private static final String USER_ID = "X-USER-ID";
    private static final String ROOM_ID = "X-ROOM-ID";
    private static final String URL = "/api/v1/shooting";

    @BeforeEach
    private void shootingAndAllReceive() throws Exception {
        long amount = 10000L;
        int count = 3;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        Room room = roomRepository.findTop1ByOrderById();

        this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk());

        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        this.mockMvc.perform(get("/api/v1/money")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/v1/money")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 3L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/v1/money")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 4L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("조회_성공")
    public void getInfo_success() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(testService.isSuccess(result));

        ShootingInfoDto dto = modelMapper.map(testService.getResponse(result), ShootingInfoDto.class);
        assertEquals(dto.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                shooting.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        assertEquals(dto.getTotalAmount(), shooting.getTotalAmount());

        long receivedAmount = dto.getReceiveList().stream().mapToLong(ReceiveMoneyDto::getAmount).sum();
        assertEquals(receivedAmount, dto.getTotalAmount());
        assertEquals(receivedAmount, shooting.getTotalAmount());
    }

    @Test
    @DisplayName("조회_실패 : token 길이는 3이어야 합니다")
    public void getInfo_fail_1() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", "test"))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, "token 길이는 3이어야 합니다."));
    }

    @Test
    @DisplayName("조회_실패 : 잘못된 대화방 입니다")
    public void getInfo_fail_2() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, "UUID!")
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, "잘못된 대화방 입니다."));
    }

    @Test
    @DisplayName("조회_실패 : 잘못된 유저 또는 대화방 입니다")
    public void getInfo_fail_3() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1000L)
                .header(ROOM_ID, "f0707272-a5b5-4f72-a4dc-5325b534eb1c")
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, "잘못된 유저 또는 대화방 입니다."));
    }

    @Test
    @DisplayName("조회_실패 : 잘못된 token 입니다")
    public void getInfo_fail_4() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", "o-o"))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, "잘못된 token 입니다."));
    }

    @Test
    @DisplayName("조회_실패 : 본인이 뿌린 건만 조회할 수 있습니다")
    public void getInfo_fail_5() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
        assertTrue(testService.isErrorMessage(result, "본인이 뿌린 건만 조회할 수 있습니다."));
    }

    @Test
    @DisplayName("조회_실패 : 뿌린 건에 대한 조회는 7일 동안만 할 수 있습니다")
    public void getInfo_fail_6() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();
        shooting.changeCreateDateOnlyTest(LocalDateTime.now().minusDays(8));

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
        assertTrue(testService.isErrorMessage(result, "뿌린 건에 대한 조회는 7일 동안만 할 수 있습니다."));
    }

}
