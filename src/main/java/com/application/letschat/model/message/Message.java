package com.application.letschat.model.message;

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
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sender_id", nullable = true) // Allowed null for system messages
    private User user;

    @Column(name="content", length = 255)
    private String content;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Timestamp enrolledAt;

    // Set enrolledAt to NOW() only if it's null
    @PrePersist
    protected void onCreate() {
        if (this.enrolledAt == null) {
            this.enrolledAt = new Timestamp(System.currentTimeMillis());
        }
    }
}
