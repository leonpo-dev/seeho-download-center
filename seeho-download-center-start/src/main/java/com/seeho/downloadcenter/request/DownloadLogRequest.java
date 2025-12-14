package com.seeho.downloadcenter.request;

import com.seeho.downloadcenter.base.model.DownloadColumnDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DownloadLogRequest {

    @NotBlank(message = "downloadName must not be blank")
    @Size(max = 50, message = "downloadName must be at most 50 characters")
    private String downloadName;

    /**
     * @see com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum
     */
    @NotBlank(message = "downloadEnum must not be blank")
    private String downloadEnum;

    @NotBlank(message = "downloadCondition must not be blank")
    private String downloadCondition;

    /** Dynamic column configuration supplied by the client. */
    private List<DownloadColumnDTO> titles;
}
