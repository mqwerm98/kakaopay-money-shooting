package kakaopay.money.service;

import kakaopay.money.dto.CreateShootingDto;
import kakaopay.money.dto.ReceiveMoneyDto;
import kakaopay.money.dto.ShootingInfoDto;
import kakaopay.money.entity.Receive;
import kakaopay.money.entity.Room;
import kakaopay.money.entity.Shooting;
import kakaopay.money.entity.User;
import kakaopay.money.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShootingService {

    private final ShootingRepository shootingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomUserRepository roomUserRepository;
    private final ReceiveRepository receiveRepository;

    public UUID stringToUUID(String uuid) {
        try {
            return UUID.fromString(
                    uuid.replaceFirst(
                            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean existUserRoom(long userId, UUID roomId) {
        if (!userRepository.existsById(userId)) return false;

        return roomRepository.existsById(roomId);
    }

    public boolean checkUserInRoom(long userId, UUID roomId) {
        return roomUserRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    public boolean checkShootingCount(UUID roomId, int shootingCount) {
        int userCount = roomUserRepository.countByRoomId(roomId);
        return userCount >= shootingCount;
    }

    @Transactional
    public String shoot(Long userId, UUID roomId, CreateShootingDto dto) {
        User user = userRepository.findById(userId).get();
        Room room = roomRepository.findById(roomId).get();


        long[] amounts = distribute(dto.getAmount(), dto.getCount());

        Shooting shooting = new Shooting(room, user, dto.getAmount(), dto.getCount());

        for (int i = 0; i < dto.getCount(); i++) {
            Receive receive = new Receive(shooting, amounts[i]);
            receiveRepository.save(receive);
        }

        return shooting.getToken();
    }

    private long[] distribute(long amount, int count) {
        int[] dis = new int[count];

        int disSum = 0;
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            dis[i] = random.nextInt(100000) + 1;
            disSum += dis[i];
        }

        long[] amounts = new long[count];
        long amountSum = 0L;
        for (int i = 0; i < count; i++) {
            amounts[i] = (long) (dis[i] / (float) disSum * amount);
            amountSum += amounts[i];
        }

        if(amountSum != amount) {
            amounts[random.nextInt(count)] += (amount - amountSum);
        }

        return amounts;
    }

    public boolean checkReceived(Shooting shooting, User user) {
        return receiveRepository.existsByShootingAndUser(shooting, user);
    }

    @Transactional
    public long receive(Shooting shooting, User user) {
        Receive receive = receiveRepository.findTop1ByShootingAndReceivedOrderByIdAsc(shooting, false);
        return receive.get(user);
    }

    public ShootingInfoDto getInfo(Shooting shooting) {
        List<Receive> receiveList = receiveRepository.findByShootingAndReceived(shooting, true);
        long receivedAmount = ((LongStream) receiveList.stream().map(Receive::getAmount)).sum();

        List<ReceiveMoneyDto> receiveMoneyDtoList = receiveList.stream()
                .map(r -> new ReceiveMoneyDto(r.getAmount(), r.getUser().getId()))
                .collect(Collectors.toList());

        ShootingInfoDto dto = ShootingInfoDto.builder()
                .createDate(shooting.getCreateDate())
                .totalAmount(shooting.getTotalAmount())
                .receivedAmount(receivedAmount)
                .receiveList(receiveMoneyDtoList)
                .build();

        return dto;
    }
}
