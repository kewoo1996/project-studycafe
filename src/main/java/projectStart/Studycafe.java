package projectStart;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Studycafe_table")
public class Studycafe {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long customerId;
    private String seatCount;

    @PostPersist
    public void onPostPersist(){
        SeatCounted seatCounted = new SeatCounted();
        BeanUtils.copyProperties(this, seatCounted);
        seatCounted.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(String seatCount) {
        this.seatCount = seatCount;
    }




}
