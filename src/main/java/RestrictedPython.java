public class RestrictedPython {
    public static String getSafeCode(String code){
        String safeCode="";
        safeCode += "from RestrictedPython import compile_restricted;";
        safeCode += "from RestrictedPython import safe_globals;";
        //safeCode += "loc={};";
        safeCode+= "safe_globals['print'] = print;";
        safeCode+= "safe_globals['__builtins__']['__import__'] = __import__;";
        safeCode += "code=\"" + code.replace("\"", "\\\"") + "\";";
        safeCode += "byte_code=compile(code, '<inline>', 'exec');";
        safeCode += "exec(byte_code, safe_globals, loc);";
        safeCode += "locals().update(loc);";
        //System.out.println(safeCode);
        return code;
    }

}
