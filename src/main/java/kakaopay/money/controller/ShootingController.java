package kakaopay.money.controller;

import kakaopay.money.dto.CreateShootingDto;
import kakaopay.money.dto.ShootingInfoDto;
import kakaopay.money.dto.response.Response;
import kakaopay.money.entity.Shooting;
import kakaopay.money.entity.User;
import kakaopay.money.repository.ShootingRepository;
import kakaopay.money.repository.UserRepository;
import kakaopay.money.service.ShootingService;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ShootingController {

    private static final String USER_ID = "X-USER-ID";
    private static final String ROOM_ID = "X-ROOM-ID";

    private final ShootingService shootingService;
    private final ShootingRepository shootingRepository;
    private final UserRepository userRepository;

    @PostMapping("/shooting")
    public Response shooting(@RequestHeader(USER_ID) Long userId,
                             @RequestHeader(ROOM_ID) String roomId,
                             @Valid @RequestBody CreateShootingDto dto, Errors errors) {

        if (errors.hasErrors()) {
            return Response.BAD_REQUEST_ERROR(errors);
        }

        if (dto.getAmount() < dto.getCount()) {
            return Response.BAD_REQUEST_ERROR("뿌릴 금액은 뿌릴 인원수보다 커야합니다.");
        }

        UUID roomUUID = shootingService.stringToUUID(roomId);
        if (roomUUID == null) {
            return Response.BAD_REQUEST_ERROR("잘못된 대화방 입니다.");
        }

        if (!shootingService.existUserRoom(userId, roomUUID)) {
            return Response.BAD_REQUEST_ERROR("잘못된 유저 또는 대화방 입니다.");
        }

        if (!shootingService.checkUserInRoom(userId, roomUUID)) {
            return Response.ERROR("뿌리기는 참여중인 대화방 에서만 가능합니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        if (!shootingService.checkShootingCount(roomUUID, dto.getCount())) {
            return Response.ERROR("뿌릴 인원수의 최대값은 대화방 인원수 입니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        String token = shootingService.shoot(userId, roomUUID, dto);

        return Response.OK(token);
    }

    @GetMapping("/money")
    public Response receiveMoney(@RequestHeader(USER_ID) Long userId,
                             @RequestHeader(ROOM_ID) String roomId,
                             @RequestParam("token") String token) {

        if (token.length() != 3) {
            return Response.BAD_REQUEST_ERROR("token 길이는 3이어야 합니다.");
        }

        UUID roomUUID = shootingService.stringToUUID(roomId);
        if (roomUUID == null) {
            return Response.BAD_REQUEST_ERROR("잘못된 대화방 입니다.");
        }

        if (!shootingService.existUserRoom(userId, roomUUID)) {
            return Response.BAD_REQUEST_ERROR("잘못된 유저 또는 대화방 입니다.");
        }

        if (!shootingService.checkUserInRoom(userId, roomUUID)) {
            return Response.ERROR("참여중인 대화방 에서만 가능합니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        Shooting shooting = shootingRepository.findByRoomIdAndToken(roomUUID, token);

        if (shooting == null) {
            return Response.BAD_REQUEST_ERROR("잘못된 token 입니다.");
        }

        if (shooting.getCreateDate().plusMinutes(10).isBefore(LocalDateTime.now())) {
            return Response.ERROR("종료된 뿌리기 입니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        User user = userRepository.findById(userId).get();
        if (shooting.getUser().equals(user)) {
            return Response.ERROR("자신이 뿌린건 받을 수 없습니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        if (shootingService.checkReceived(shooting, user)) {
            return Response.ERROR("이미 받았습니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        long amount = shootingService.receive(shooting, user);
        return Response.OK(amount);
    }

    @GetMapping("/shooting")
    public Response getShootingInfo(@RequestHeader(USER_ID) Long userId,
                             @RequestHeader(ROOM_ID) String roomId,
                             @RequestParam("token") String token) {

        if (token.length() != 3) {
            return Response.BAD_REQUEST_ERROR("token 길이는 3이어야 합니다.");
        }

        UUID roomUUID = shootingService.stringToUUID(roomId);
        if (roomUUID == null) {
            return Response.BAD_REQUEST_ERROR("잘못된 대화방 입니다.");
        }

        if (!shootingService.existUserRoom(userId, roomUUID)){
            return Response.BAD_REQUEST_ERROR("잘못된 유저 또는 대화방 입니다.");
        }

        Shooting shooting = shootingRepository.findByRoomIdAndToken(roomUUID, token);

        if (shooting == null) {
            return Response.BAD_REQUEST_ERROR("잘못된 token 입니다.");
        }

        User user = userRepository.findById(userId).get();

        if (!shooting.getUser().equals(user)) {
            return Response.ERROR("본인이 뿌린 건만 조회할 수 있습니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        if (shooting.getCreateDate().plusDays(7).isBefore(LocalDateTime.now())) {
            return Response.ERROR("뿌린 건에 대한 조회는 7일 동안만 할 수 있습니다.", HttpStatus.NOT_ACCEPTABLE);
        }

        ShootingInfoDto dto = shootingService.getInfo(shooting);

        return Response.OK(dto);
    }
}
