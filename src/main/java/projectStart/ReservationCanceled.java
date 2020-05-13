
package projectStart;

public class ReservationCanceled extends AbstractEvent {

    private Long id;
    private Long cutomerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCutomerId() {
        return cutomerId;
    }

    public void setCutomerId(Long cutomerId) {
        this.cutomerId = cutomerId;
    }
}
