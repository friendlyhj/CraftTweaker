package com.blamejared.crafttweaker.api.data;

import com.blamejared.crafttweaker.impl.data.BoolData;
import com.blamejared.crafttweaker.impl.data.ByteData;
import com.blamejared.crafttweaker.impl.data.DoubleData;
import com.blamejared.crafttweaker.impl.data.FloatData;
import com.blamejared.crafttweaker.impl.data.IntData;
import com.blamejared.crafttweaker.impl.data.ListData;
import com.blamejared.crafttweaker.impl.data.LongData;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.data.ShortData;
import com.blamejared.crafttweaker.impl.data.StringData;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.LiteralSourceFile;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.lexer.ZSToken;
import org.openzen.zenscript.lexer.ZSTokenParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openzen.zenscript.lexer.ZSTokenType.*;

public class StringConverter {
    
    public static IData convert(String expression) throws ParseException {
        
        try {
            ZSTokenParser parser = ZSTokenParser.create(new LiteralSourceFile("", expression), null);
            return parse(parser);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static IData parse(ZSTokenParser parser) throws ParseException {
        
        ZSToken next = parser.next();
        final IData base;
        switch(next.getType()) {
            case T_AOPEN:
                base = parseMap(parser);
                break;
            case T_SQOPEN:
                base = parseList(parser);
                break;
            case K_TRUE:
                base = new BoolData(true);
                break;
            case K_FALSE:
                base = new BoolData(false);
                break;
            case T_INT:
                base = new LongData(Long.parseLong(next.getContent(), 10));
                break;
            case T_FLOAT:
                base = new DoubleData(Double.parseDouble(next.getContent()));
                break;
            case T_STRING_DQ:
            case T_STRING_DQ_WYSIWYG:
                base = new StringData(next.getContent());
                break;
            default:
                throw new ParseException(CodePosition.META, "Could not completely resolve Data near " + next.toString());
        }
        
        if(parser.optional(K_AS) != null) {
            final ZSToken token = parser.next();
            switch(token.getType()) {
                case K_BOOL:
                    return new BoolData(base.asNumber().getInt() == 1);
                case K_BYTE:
                    return new ByteData(base.asNumber().getByte());
                case K_SHORT:
                    return new ShortData(base.asNumber().getShort());
                case K_INT:
                    return new IntData(base.asNumber().getInt());
                case K_LONG:
                    return new LongData(base.asNumber().getLong());
                case K_FLOAT:
                    return new FloatData(base.asNumber().getFloat());
                case K_DOUBLE:
                    return new DoubleData(base.asNumber().getDouble());
                case K_STRING:
                    if(base instanceof StringData) {
                        return base;
                    }
                    return new StringData(base.toJsonString());
                case T_IDENTIFIER:
                    return base;
            }
        }
        return base;
    }
    
    private static IData parseList(ZSTokenParser parser) throws ParseException {
        
        final List<IData> result = new ArrayList<>();
        while(parser.optional(T_SQCLOSE) == null) {
            result.add(parse(parser));
            if(parser.optional(T_COMMA) == null) {
                parser.required(T_SQCLOSE, "] expected");
                break;
            }
        }
        return new ListData(result);
    }
    
    private static IData parseMap(ZSTokenParser parser) throws ParseException {
        
        final Map<String, IData> result = new HashMap<>();
        while(parser.optional(T_ACLOSE) == null) {
            final String key = parser.required(T_IDENTIFIER, "Identifier expected").getContent();
            parser.required(T_COLON, ": expected");
            result.put(key, parse(parser));
            
            if(parser.optional(T_COMMA) == null) {
                parser.required(T_ACLOSE, "} expected");
                break;
            }
        }
        return new MapData(result);
    }
    
}
