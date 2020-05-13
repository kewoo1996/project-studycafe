package projectStart;

import projectStart.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired StudycafeRepository studycafeRepository;
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_Count(@Payload ReservationCanceled reservationCanceled){

        //자리 취소
        if(reservationCanceled.isMe()){
            //studycafeRepository.deleteByCustomerId(reservationCanceled.getCutomerId());
            System.out.println("##### listener Count Delete : " + reservationCanceled.toJson());
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_Count(@Payload Paid paid){
        //자리 등록
        if(paid.isMe()){
            Studycafe studycafe = new Studycafe();
            studycafe.setCustomerId(paid.getCustomerId());
            studycafeRepository.save(studycafe);
            System.out.println("##### listener Count : " + paid.toJson());
        }
    }
}
