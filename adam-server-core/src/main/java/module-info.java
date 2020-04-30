module org.caichongjian.adam.server.core {
    requires org.slf4j;
    requires org.apache.commons.collections4;
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires fastjson;
    requires java.servlet;
    requires org.caichongjian.mini.servlet.api;

    exports org.caichongjian.server.startup;
}