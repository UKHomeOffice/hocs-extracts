package uk.gov.digital.ho.hocs.extracts.service.domain.adapter;

public interface ExportViewFieldAdapter {

    String getAdapterType();

    String convert(Object input);
}
