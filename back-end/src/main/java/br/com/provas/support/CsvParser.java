package br.com.provas.support;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.LinhaCsv;
import java.util.ArrayList;
import java.util.List;

/** Leitura de CSV com aspas escapadas, separador vírgula/ponto e vírgula e número da linha física. */
public final class CsvParser {
    private CsvParser() {}
    /** @param input CSV em texto @return linhas não vazias, incluindo o cabeçalho */
    public static List<LinhaCsv> parse(String input) {
        String text=input.startsWith("\uFEFF")?input.substring(1):input;
        text=text.replace("\r\n","\n").replace('\r','\n');
        int initialLine=1;
        if(text.startsWith("sep=;\n") || text.startsWith("sep=,\n")){text=text.substring(6);initialLine=2;}
        String header=text.split("\n",2)[0];
        char delimiter=count(header,';')>=count(header,',')?';':',';
        var rows=new ArrayList<LinhaCsv>();var fields=new ArrayList<String>();var field=new StringBuilder();
        boolean quoted=false,closed=false;int line=initialLine,start=initialLine;
        for(int i=0;i<text.length();i++) {
            char ch=text.charAt(i);
            if(quoted) {
                if(ch=='"') {
                    if(i+1<text.length() && text.charAt(i+1)=='"'){field.append('"');i++;}
                    else {quoted=false;closed=true;}
                } else {field.append(ch);if(ch=='\n')line++;}
            } else if(ch==delimiter) {
                fields.add(field.toString());field.setLength(0);closed=false;
            } else if(ch=='\n') {
                fields.add(field.toString());add(rows,start,fields);fields=new ArrayList<>();field.setLength(0);closed=false;line++;start=line;
            } else if(closed) {
                if(ch!=' ' && ch!='\t') throw invalid(line);
            } else if(ch=='"') {
                if(!field.toString().isBlank()) throw invalid(line);
                field.setLength(0);quoted=true;
            } else field.append(ch);
        }
        if(quoted)throw new DefaultErrorException("CSV inválido na linha "+start+": aspas não foram fechadas.");
        fields.add(field.toString());add(rows,start,fields);return rows;
    }
    private static int count(String text,char delimiter) {
        int count=0;boolean quoted=false;
        for(int i=0;i<text.length();i++){char ch=text.charAt(i);if(ch=='"')quoted=!quoted;else if(ch==delimiter&&!quoted)count++;}
        return count;
    }
    private static void add(List<LinhaCsv> rows,int line,List<String> fields) {
        if(fields.stream().anyMatch(f->!f.isBlank()))rows.add(new LinhaCsv(line,List.copyOf(fields)));
        if(rows.size()>1001)throw new DefaultErrorException("O CSV deve conter no máximo 1.000 alunos.");
    }
    private static DefaultErrorException invalid(int line) { return new DefaultErrorException("CSV inválido na linha "+line+": confira as aspas e os separadores."); }
}
