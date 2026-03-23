package org.ecommerce.common.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class ProductUploadFormDto {
    @RestForm("file")
    public FileUpload file;
}