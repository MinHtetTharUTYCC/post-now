package com.minhtetthar.post_now.mapper;

import com.minhtetthar.post_now.dto.comment.CommentCreateDto;
import com.minhtetthar.post_now.dto.comment.CommentDto;
import com.minhtetthar.post_now.entity.Comment;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = UserMapper.class)
public interface CommentMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "postId", source = "post.id")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "active", constant = "true")
    Comment toEntity(CommentCreateDto dto);
}