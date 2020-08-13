package kakaopay.money.repository;

import kakaopay.money.entity.RoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {
    boolean existsByUserIdAndRoomId(long userId, UUID roomId);

    int countByRoomId(UUID roomId);
}
