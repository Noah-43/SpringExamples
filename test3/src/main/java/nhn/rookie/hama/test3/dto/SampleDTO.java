package nhn.rookie.hama.test3.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter/Setter, toString(), equals(), hashCode() 자동 생성
@Builder(toBuilder = true) // builder 패턴으로 생성된 객체의 일부 값을 변경한 새로 운 객체를 생
public class SampleDTO {

    private Long sno;

    private String first;

    private String last;

    private LocalDateTime regTime;
}
