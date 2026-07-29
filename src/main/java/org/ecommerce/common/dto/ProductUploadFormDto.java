package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Getter
@Setter
public class ProductUploadFormDto
{
    @RestForm("file")
    private FileUpload file;
}