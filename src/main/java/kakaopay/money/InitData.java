package kakaopay.money;

import kakaopay.money.entity.Room;
import kakaopay.money.entity.RoomUser;
import kakaopay.money.entity.User;
import kakaopay.money.repository.RoomRepository;
import kakaopay.money.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitData {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.createRoomUser();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final UserRepository userRepository;
        private final RoomRepository roomRepository;

        public void createRoomUser() {
            for (int i = 0; i < 10; i++) {
                User user = new User();
                em.persist(user);
            }

            for (int i = 0; i < 2; i++) {
                Room room = new Room();
                em.persist(room);
            }

            List<User> userList = userRepository.findAll();
            List<Room> roomList = roomRepository.findAll();

            for (int i = 0; i < 5; i++) {
                RoomUser roomUser = new RoomUser(roomList.get(0), userList.get(i));
                em.persist(roomUser);
            }

            for (int i = 5; i < 10; i++) {
                RoomUser roomUser = new RoomUser(roomList.get(1), userList.get(i));
                em.persist(roomUser);
            }
        }

    }
}
