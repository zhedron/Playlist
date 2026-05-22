package zhedron.playlist.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subscriptions")
@Data
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "subscriber_user_id")
    private User subscriber;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser;
}
