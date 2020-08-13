package kakaopay.money.controller;

import kakaopay.money.dto.CreateShootingDto;
import kakaopay.money.dto.ShootingInfoDto;
import kakaopay.money.dto.response.ErrorMessage;
import kakaopay.money.dto.response.Response;
import kakaopay.money.entity.Shooting;
import kakaopay.money.entity.User;
import kakaopay.money.repository.ShootingRepository;
import kakaopay.money.repository.UserRepository;
import kakaopay.money.service.ShootingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shooting")
public class ShootingController {

    private static final String USER_ID = "X-USER-ID";
    private static final String ROOM_ID = "X-ROOM-ID";

    private final ShootingService shootingService;
    private final ShootingRepository shootingRepository;
    private final UserRepository userRepository;

    @PostMapping
    public Response shooting(@RequestHeader(USER_ID) Long userId,
                             @RequestHeader(ROOM_ID) String roomId,
                             @Valid @RequestBody CreateShootingDto dto, Errors errors) {

        if (errors.hasErrors()) {
            return Response.BAD_REQUEST_ERROR(errors);
        }

        if (dto.getAmount() < dto.getCount()) {
            return Response.ERROR(ErrorMessage.E001);
        }

        UUID roomUUID = shootingService.stringToUUID(roomId);

        ErrorMessage message = checkUserRoom(userId, roomUUID);
        if (message != null) return Response.ERROR(message);

        if (!shootingService.checkUserInRoom(userId, roomUUID)) {
            return Response.ERROR(ErrorMessage.E004);
        }

        if (!shootingService.checkShootingCount(roomUUID, dto.getCount())) {
            return Response.ERROR(ErrorMessage.E005);
        }

        String token = shootingService.shoot(userId, roomUUID, dto);

        return Response.OK(token);
    }

    @PutMapping
    public Response receiveMoney(@RequestHeader(USER_ID) Long userId,
                             @RequestHeader(ROOM_ID) String roomId,
                             @RequestParam("token") String token) {

        if (token.length() != 3) {
            return Response.ERROR(ErrorMessage.E006);
        }

        UUID roomUUID = shootingService.stringToUUID(roomId);

        ErrorMessage message = checkUserRoom(userId, roomUUID);
        if (message != null) return Response.ERROR(message);

        if (!shootingService.checkUserInRoom(userId, roomUUID)) {
            return Response.ERROR(ErrorMessage.E007);
        }

        Shooting shooting = shootingRepository.findByRoomIdAndToken(roomUUID, token);

        if (shooting == null) {
            return Response.ERROR(ErrorMessage.E008);
        }

        if (shooting.getCreateDate().plusMinutes(10).isBefore(LocalDateTime.now())) {
            return Response.ERROR(ErrorMessage.E009);
        }

        User user = userRepository.findById(userId).get();
        if (shooting.getUser().equals(user)) {
            return Response.ERROR(ErrorMessage.E010);
        }

        if (shootingService.checkReceived(shooting, user)) {
            return Response.ERROR(ErrorMessage.E011);
        }

        long amount = shootingService.receive(shooting, user);
        return Response.OK(amount);
    }

    @GetMapping
    public Response getShootingInfo(@RequestHeader(USER_ID) Long userId,
                             @RequestHeader(ROOM_ID) String roomId,
                             @RequestParam("token") String token) {

        if (token.length() != 3) return Response.ERROR(ErrorMessage.E006);

        UUID roomUUID = shootingService.stringToUUID(roomId);

        ErrorMessage message = checkUserRoom(userId, roomUUID);
        if (message != null) return Response.ERROR(message);

        Shooting shooting = shootingRepository.findByRoomIdAndToken(roomUUID, token);

        if (shooting == null) {
            return Response.ERROR(ErrorMessage.E008);
        }

        User user = userRepository.findById(userId).get();

        if (!shooting.getUser().equals(user)) {
            return Response.ERROR(ErrorMessage.E012);
        }

        if (shooting.getCreateDate().plusDays(7).isBefore(LocalDateTime.now())) {
            return Response.ERROR(ErrorMessage.E013);
        }

        ShootingInfoDto dto = shootingService.getInfo(shooting);

        return Response.OK(dto);
    }

    private ErrorMessage checkUserRoom(@RequestHeader(USER_ID) Long userId, UUID roomUUID) {
        if (roomUUID == null) {
            return ErrorMessage.E002;
        }

        if (!shootingService.existUserRoom(userId, roomUUID)) {
            return ErrorMessage.E003;
        }

        return null;
    }
}
