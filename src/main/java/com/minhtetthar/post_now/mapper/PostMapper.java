package com.minhtetthar.post_now.mapper;

import com.minhtetthar.post_now.dto.post.PostCreateDto;
import com.minhtetthar.post_now.dto.post.PostDto;
import com.minhtetthar.post_now.dto.post.PostUpdateDto;
import com.minhtetthar.post_now.entity.Post;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = UserMapper.class)
public interface PostMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "commentsCount", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    PostDto toDto(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "active", constant = "true")
    Post toEntity(PostCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likes", ignore = true)
    void updateEntity(@MappingTarget Post post, PostUpdateDto dto);
}