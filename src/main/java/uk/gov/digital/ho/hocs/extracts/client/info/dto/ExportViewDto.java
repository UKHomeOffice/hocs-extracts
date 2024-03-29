package uk.gov.digital.ho.hocs.extracts.client.info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExportViewDto {

    private Long id;
    private String code;
    private String displayName;
    private String requiredPermission;
    private List<ExportViewFieldDto> fields;
}
