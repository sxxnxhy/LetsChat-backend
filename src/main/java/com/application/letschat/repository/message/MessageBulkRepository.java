package com.application.letschat.repository.message;

import com.application.letschat.model.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MessageBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsertMessages(List<Message> messages) {
        String sql = "INSERT INTO message (chat_room_id, sender_id, content, enrolled_at) " +
                "VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Message message = messages.get(i);

                ps.setLong(1, message.getChatRoom().getChatRoomId());
                if (message.getUser() != null) {
                    ps.setLong(2, message.getUser().getUserId());
                } else {
                    ps.setNull(2, java.sql.Types.BIGINT);
                }
                ps.setString(3, message.getContent());
                ps.setTimestamp(4, message.getEnrolledAt());
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

}
