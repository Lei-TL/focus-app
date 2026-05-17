package org.resourceserver.modules.user.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ChangeAvatarRequest {

    private MultipartFile avatar;

}
