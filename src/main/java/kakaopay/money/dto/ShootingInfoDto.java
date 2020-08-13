package kakaopay.money.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShootingInfoDto {

    private LocalDateTime createDate;

    private long totalAmount;

    private long receivedAmount;

    private List<ReceiveMoneyDto> receiveList;

}
