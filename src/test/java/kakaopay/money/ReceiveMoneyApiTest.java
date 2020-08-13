package kakaopay.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import kakaopay.money.dto.CreateShootingDto;
import kakaopay.money.entity.Receive;
import kakaopay.money.entity.Room;
import kakaopay.money.entity.Shooting;
import kakaopay.money.repository.ReceiveRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ReceiveMoneyApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private TestService testService;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private ModelMapper modelMapper;

    @Autowired private RoomRepository roomRepository;

    @Autowired private ShootingRepository shootingRepository;

    @Autowired private ReceiveRepository receiveRepository;

    private static final String USER_ID = "X-USER-ID";
    private static final String ROOM_ID = "X-ROOM-ID";
    private static final String URL = "/api/v1/money";

    @BeforeEach
    private void shooting() throws Exception {
        long amount = 10000;
        int count = 5;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        Room room = roomRepository.findTop1ByOrderById();

        this.mockMvc.perform(post("/api/v1/shooting")
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("돈받기_성공")
    public void receiveMoney_success() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(testService.isSuccess(result));

        List<Receive> receiveList = receiveRepository.findByShootingAndReceived(shooting, true);
        assertEquals(receiveList.size(), 1);
        assertEquals(receiveList.get(0).getUser().getId(), 2L);
    }

    @Test
    @DisplayName("돈받기_실패 : token 길이는 3이어야 합니다")
    public void receiveMoney_fail_1() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", "test"))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
    }

    @Test
    @DisplayName("돈받기_실패 : 잘못된 대화방 입니다")
    public void receiveMoney_fail_2() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, "UUID!")
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
    }

    @Test
    @DisplayName("돈받기_실패 : 잘못된 유저 또는 대화방 입니다")
    public void receiveMoney_fail_3() throws Exception {
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
    }

    @Test
    @DisplayName("돈받기_실패 : 참여중인 대화방 에서만 가능합니다")
    public void receiveMoney_fail_4() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 6L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
    }

    @Test
    @DisplayName("돈받기_실패 : 잘못된 token 입니다")
    public void receiveMoney_fail_5() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", "o-o"))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
    }

    @Test
    @DisplayName("돈받기_실패 : 종료된 뿌리기 입니다")
    public void receiveMoney_fail_6() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();
        shooting.changeCreateDateOnlyTest(LocalDateTime.now().minusMinutes(11));

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
    }

    @Test
    @DisplayName("돈받기_실패 : 자신이 뿌린건 받을 수 없습니다")
    public void receiveMoney_fail_7() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
    }

    @Test
    @DisplayName("돈받기_실패 : 이미 받았습니다")
    public void receiveMoney_fail_8() throws Exception {
        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();

        MvcResult result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(testService.isSuccess(result));

        result = this.mockMvc.perform(get(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 2L)
                .header(ROOM_ID, shooting.getRoom().getId().toString())
                .param("token", shooting.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
    }

}
