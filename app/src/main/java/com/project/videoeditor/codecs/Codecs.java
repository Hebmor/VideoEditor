package com.project.videoeditor.codecs;
public class Codecs {
    public enum CodecsName {
        VP9,
        H264,
        AV1
    }
    public static String toStringFFMPEGName(CodecsName name) throws Exception {
        switch (name)
        {
            case VP9:
                return "libvpx-vp9";
            case AV1:
                return "libaom-av1";
            case H264:
                return "libx264";
            default:
                throw new Exception("Неизвестное название кодека!");
        }
    }
    public static Codecs.CodecsName fromString(String codecName) throws Exception {
        switch (codecName)
        {
            case "VP9":
                return CodecsName.VP9;
            case "AV1":
                return CodecsName.AV1;
            case "H264":
                return CodecsName.H264;
            default:
                throw new Exception("Неизвестное название кодека!");
        }
    }
}
