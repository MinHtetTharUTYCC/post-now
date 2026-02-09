package com.minhtetthar.post_now.dto.follow;

import com.minhtetthar.post_now.dto.user.UserSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowDto {
    private Long id;
    private UserSummaryDto follower;
    private UserSummaryDto following;
    private LocalDateTime createdAt;
}
