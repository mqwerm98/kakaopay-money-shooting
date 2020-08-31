package kakaopay.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import kakaopay.money.dto.CreateShootingDto;
import kakaopay.money.dto.response.ErrorMessage;
import kakaopay.money.entity.*;
import kakaopay.money.repository.ReceiveRepository;
import kakaopay.money.repository.RoomRepository;
import kakaopay.money.repository.ShootingRepository;
import kakaopay.money.service.TestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kakaopay.money.controller.ShootingController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ShootingApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private TestService testService;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private RoomRepository roomRepository;

    @Autowired private ShootingRepository shootingRepository;

    @Autowired private ReceiveRepository receiveRepository;

    @Test
    @DisplayName("돈뿌리기_성공")
    public void shooting_success() throws Exception {
        long amount = 1000000000L;
        int count = 5;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        Room room = roomRepository.findTop1ByOrderById();

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(testService.isSuccess(result));

        String token = (String) testService.getResponse(result);

        Shooting shooting = shootingRepository.findTop1ByOrderByCreateDateDesc();
        assertEquals(shooting.getUser().getId(), 1L);
        assertEquals(shooting.getRoom(), room);
        assertEquals(shooting.getToken(), token);

        List<Receive> receiveList = receiveRepository.findByShooting(shooting);

        assertEquals(receiveList.size(), count);
        assertEquals(receiveList.stream().mapToLong(Receive::getAmount).sum(), amount);
    }

    @Test
    @DisplayName("돈뿌리기_실패 : DTO valid check")
    public void shooting_fail_1() throws Exception {
        String json = objectMapper.writeValueAsString(new CreateShootingDto());

        Room room = roomRepository.findTop1ByOrderById();

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
    }

    @Test
    @DisplayName("돈뿌리기_실패 : E001")
    public void shooting_fail_E001() throws Exception {
        long amount = 4L;
        int count = 5;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        Room room = roomRepository.findTop1ByOrderById();

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, ErrorMessage.E001));
    }

    @Test
    @DisplayName("돈뿌리기_실패 : E002")
    public void shooting_fail_E002() throws Exception {
        long amount = 10000L;
        int count = 5;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, "UUID!")
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, ErrorMessage.E002));
    }

    @Test
    @DisplayName("돈뿌리기_실패 : E003")
    public void shooting_fail_E003() throws Exception {
        long amount = 10000L;
        int count = 5;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1000L)
                .header(ROOM_ID, "f0707272-a5b5-4f72-a4dc-5325b534eb1c")
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isBadRequest(result));
        assertTrue(testService.isErrorMessage(result, ErrorMessage.E003));
    }

    @Test
    @DisplayName("돈뿌리기_실패 : E004")
    public void shooting_fail_E004() throws Exception {
        long amount = 10000L;
        int count = 5;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        Room room = roomRepository.findTop1ByOrderById();

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 6L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
        assertTrue(testService.isErrorMessage(result, ErrorMessage.E004));
    }

    @Test
    @DisplayName("돈뿌리기_실패 : E005")
    public void shooting_fail_E005() throws Exception {
        long amount = 10000L;
        int count = 6;
        CreateShootingDto dto = new CreateShootingDto(amount, count);
        String json = objectMapper.writeValueAsString(dto);

        Room room = roomRepository.findTop1ByOrderById();

        MvcResult result = this.mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(USER_ID, 1L)
                .header(ROOM_ID, room.getId().toString())
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        assertFalse(testService.isSuccess(result));
        assertTrue(testService.isNotAcceptable(result));
        assertTrue(testService.isErrorMessage(result, ErrorMessage.E005));
    }

}
