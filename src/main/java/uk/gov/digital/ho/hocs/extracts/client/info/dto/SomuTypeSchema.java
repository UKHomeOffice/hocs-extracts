package uk.gov.digital.ho.hocs.extracts.client.info.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SomuTypeSchema {
    List<SomuTypeField> fields;
}
