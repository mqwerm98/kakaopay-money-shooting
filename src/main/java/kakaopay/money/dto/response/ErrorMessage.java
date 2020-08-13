package kakaopay.money.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorMessage {

    E001("뿌릴 금액은 뿌릴 인원수보다 커야합니다.", HttpStatus.BAD_REQUEST),
    E002("잘못된 대화방 입니다.", HttpStatus.BAD_REQUEST),
    E003("잘못된 유저 또는 대화방 입니다.", HttpStatus.BAD_REQUEST),
    E004("뿌리기는 참여중인 대화방 에서만 가능합니다.", HttpStatus.NOT_ACCEPTABLE),
    E005("뿌릴 인원수의 최대값은 대화방 인원수 입니다.", HttpStatus.NOT_ACCEPTABLE),
    E006("token 길이는 3이어야 합니다.", HttpStatus.BAD_REQUEST),
    E007("참여중인 대화방 에서만 가능합니다.", HttpStatus.NOT_ACCEPTABLE),
    E008("잘못된 token 입니다.", HttpStatus.BAD_REQUEST),
    E009("종료된 뿌리기 입니다.", HttpStatus.NOT_ACCEPTABLE),
    E010("자신이 뿌린건 받을 수 없습니다.", HttpStatus.NOT_ACCEPTABLE),
    E011("이미 받았습니다", HttpStatus.NOT_ACCEPTABLE),
    E012("본인이 뿌린 건만 조회할 수 있습니다.", HttpStatus.NOT_ACCEPTABLE),
    E013("뿌린 건에 대한 조회는 7일 동안만 할 수 있습니다.", HttpStatus.NOT_ACCEPTABLE);

    public final String value;
    public final HttpStatus status;

}
