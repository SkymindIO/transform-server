public class RestrictedPython {
    public static String getSafeCode(String code){
        String safeCode="";
        safeCode += "from RestrictedPython import compile_restricted;";
        safeCode += "from RestrictedPython import safe_globals;";
        safeCode += "loc={};";
        safeCode += "code=\"" + code.replace("\"", "\\\"") + "\";";
        safeCode += "byte_code=compile_restricted(code, '<inline>', 'exec');";
        safeCode += "exec(byte_code, safe_globals, loc);";
        safeCode += "locals().update(loc);";
        System.out.println(safeCode);
        return safeCode;
    }

}
