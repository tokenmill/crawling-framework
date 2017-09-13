package lt.tokenmill.crawling.adminui.utils;

import com.google.common.base.Joiner;
import com.vaadin.data.Item;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GridUtils {

    public static class StringListConverter implements Converter<String, List> {
        @Override
        public List convertToModel(String s, Class<? extends List> aClass, Locale locale) throws ConversionException {
            return new ArrayList();
        }

        @Override
        public String convertToPresentation(List list, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return Joiner.on(", ").join(list);
        }

        @Override
        public Class<List> getModelType() {
            return List.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }

    public static class UrlToLinkConverter implements Converter<String, String> {

        @Override
        public String convertToModel(String string, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return string;
        }

        @Override
        public String convertToPresentation(String string, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", string, string);
        }

        @Override
        public Class<String> getModelType() {
            return String.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }

    public static class ButtonPropertyGenerator extends PropertyValueGenerator<String> {


        private String name;

        public ButtonPropertyGenerator(String name) {
            this.name = name;
        }

        @Override
        public String getValue(Item item, Object itemId, Object propertyId) {
            return name;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }
}
