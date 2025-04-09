package com.application.letschat.repository.message;

import com.application.letschat.entity.chatroom.ChatRoom;
import com.application.letschat.entity.message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.chatRoom = :chatRoom ORDER BY m.messageId DESC LIMIT 1")
    Optional<Message> findLastMessageByChatRoom(@Param("chatRoom") ChatRoom chatRoom);

//    List<Message> findByChatRoom(ChatRoom chatRoom);

    Page<Message> findByChatRoomOrderByEnrolledAtDesc(ChatRoom chatRoom, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chatRoom = :chatRoom")
    Long countByChatRoom(@Param("chatRoom") ChatRoom chatRoom);
}

