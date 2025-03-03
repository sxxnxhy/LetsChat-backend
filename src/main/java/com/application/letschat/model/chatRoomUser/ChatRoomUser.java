package com.application.letschat.model.chatRoomUser;

import com.application.letschat.model.chatRoom.ChatRoom;
import com.application.letschat.model.user.User;
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

//    @PrePersist
//    protected void onCreate() {
//        if (this.lastReadAt == null) {
//            this.lastReadAt = new Timestamp(System.currentTimeMillis());
//        }
//    }
}
