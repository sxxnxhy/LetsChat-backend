package com.application.letschat.entity.chatroomuser;

import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatRoomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_user_id")
    private Long chatRoomUserId;

    @JoinColumn(name = "chat_room_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "last_read_at")
    private Timestamp lastReadAt;

    @Column(name="enrolled_at")
    private Timestamp enrolledAt;

    @Column(name="updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.enrolledAt == null) {
            this.enrolledAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
