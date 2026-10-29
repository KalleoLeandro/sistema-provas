package br.com.provas.repository;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.*;
import br.com.provas.model.response.TentativaProvaResponse;
import java.time.*;
import java.util.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

/** Estado de respostas e revisão atômica, separado do conteúdo imutável para reduzir o trabalho dos scripts. */
@Repository
public class ProvaRedisRepository {
 private final StringRedisTemplate redis;private final ObjectMapper mapper;
 public ProvaRedisRepository(StringRedisTemplate redis,ObjectMapper mapper){this.redis=redis;this.mapper=mapper;}
 private String key(Long aluno,Long lib){return "esfinora:2026:prova:"+aluno+":"+lib;}
 private static final DefaultRedisScript<Long> INITIALIZE=new DefaultRedisScript<>("""
  if redis.call('HEXISTS',KEYS[1],'deadline')==1 then return 0 end
  for i=1,#ARGV,2 do redis.call('HSET',KEYS[1],ARGV[i],ARGV[i+1]) end
  return 1
  """,Long.class);
 private static final DefaultRedisScript<Long> ANSWER=new DefaultRedisScript<>("""
  if redis.call('HGET',KEYS[1],'status')~='open' then return -3 end
  local t=redis.call('TIME');local now=t[1]*1000+math.floor(t[2]/1000)
  if now>=tonumber(redis.call('HGET',KEYS[1],'deadline')) then return -2 end
  local revision=tonumber(redis.call('HGET',KEYS[1],'r:'..ARGV[1]) or '0')
  if revision~=tonumber(ARGV[2]) then return -1 end
  redis.call('HSET',KEYS[1],'a:'..ARGV[1],ARGV[3],'r:'..ARGV[1],revision+1)
  return revision+1
  """,Long.class);
 private static final DefaultRedisScript<Long> SEAL=new DefaultRedisScript<>("""
  if redis.call('HGET',KEYS[1],'status')~='open' then return 0 end
  local t=redis.call('TIME');local now=t[1]*1000+math.floor(t[2]/1000)
  local deadline=tonumber(redis.call('HGET',KEYS[1],'deadline'));local expired=now>=deadline
  if ARGV[2]=='1' and not expired then return -5 end
  if not expired and ARGV[1]~='1' then
   for i=1,tonumber(redis.call('HGET',KEYS[1],'qtd') or '10') do if (redis.call('HGET',KEYS[1],'a:'..i) or ''):match('^%s*$') then return -4 end end
  end
  if expired then now=deadline end
  redis.call('HSET',KEYS[1],'status','sealed','sent',now,'auto',expired and '1' or '0')
  return 1
  """,Long.class);
 private static final DefaultRedisScript<Long> UNLOCK=new DefaultRedisScript<>("if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end return 0",Long.class);
 public Optional<EstadoProvaRedis> find(Long aluno,Long lib){
  String k=key(aluno,lib),json=redis.opsForValue().get(k+":copy");if(json==null)return Optional.empty();
  var values=redis.<String,String>opsForHash().entries(k+":state");if(!values.containsKey("deadline"))return Optional.empty();
  var copy=mapper.readValue(json,TentativaProvaResponse.class);
  var answers=new LinkedHashMap<Integer,String>();var revisions=new LinkedHashMap<Integer,Long>();
  for(int i=1;i<=copy.questoes().size();i++){answers.put(i,values.getOrDefault("a:"+i,""));revisions.put(i,Long.valueOf(values.getOrDefault("r:"+i,"0")));}
  String status=values.get("status");
  return Optional.of(new EstadoProvaRedis(Long.valueOf(values.get("id")),copy,answers,revisions,!"open".equals(status),"finished".equals(status),values.containsKey("sent")?Instant.ofEpochMilli(Long.parseLong(values.get("sent"))):null,"1".equals(values.get("auto"))));
 }
 public void initialize(TentativaProva attempt,TentativaProvaResponse copy){
  String k=key(attempt.alunoId(),attempt.liberacaoId());redis.opsForValue().setIfAbsent(k+":copy",mapper.writeValueAsString(copy));
  var fields=new ArrayList<String>(List.of("qtd",Integer.toString(copy.questoes().size()),"id",attempt.id().toString(),"deadline",Long.toString(attempt.encerraEm().toEpochMilli()),"status",attempt.submetidaEm()==null?"open":"finished"));
  if(attempt.submetidaEm()!=null){fields.add("sent");fields.add(Long.toString(attempt.submetidaEm().toEpochMilli()));}
  for(var q:copy.questoes()){fields.add("a:"+q.numero());fields.add(q.resposta()==null?"":q.resposta());fields.add("r:"+q.numero());fields.add(Long.toString(q.revisao()));}
  redis.execute(INITIALIZE,List.of(k+":state"),fields.toArray());
 }
 public long answer(Long aluno,Long lib,int number,String answer,long revision){return Objects.requireNonNull(redis.execute(ANSWER,List.of(key(aluno,lib)+":state"),Integer.toString(number),Long.toString(revision),answer==null?"":answer));}
 public long seal(Long aluno,Long lib,boolean confirm,boolean automatic){return Objects.requireNonNull(redis.execute(SEAL,List.of(key(aluno,lib)+":state"),confirm?"1":"0",automatic?"1":"0"));}
 public void finished(Long aluno,Long lib){String k=key(aluno,lib);redis.opsForHash().put(k+":state","status","finished");redis.expire(k+":state",Duration.ofDays(7));redis.expire(k+":copy",Duration.ofDays(7));}
 public String lockStart(Long aluno,Long lib){var token=UUID.randomUUID().toString();if(!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key(aluno,lib)+":start",token,Duration.ofSeconds(60))))throw new DefaultErrorException("Esta prova está sendo iniciada. Aguarde e tente novamente.",HttpStatus.CONFLICT);return token;}
 public boolean ownsStart(Long aluno,Long lib,String token){return token.equals(redis.opsForValue().get(key(aluno,lib)+":start"));}
 public void unlockStart(Long aluno,Long lib,String token){redis.execute(UNLOCK,List.of(key(aluno,lib)+":start"),token);}
}
