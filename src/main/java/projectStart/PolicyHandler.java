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
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_Count(@Payload ReservationCanceled reservationCanceled){

        if(reservationCanceled.isMe()){
            System.out.println("##### listener Count : " + reservationCanceled.toJson());
        }
    }
    @Autowired StudycafeRepository studycafeRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_Count(@Payload Paid paid){

        if(paid.isMe()){
            Studycafe studycafe = new Studycafe();
            studycafe.setCustomerId(paid.getCustomerId());
            studycafeRepository.save(studycafe);
            System.out.println("##### listener Count : " + paid.toJson());
        }
    }

}
