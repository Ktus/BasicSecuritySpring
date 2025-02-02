package com.indra.security.model.service.auth;
import static com.indra.security.util.Constants.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.indra.security.model.service.CacheServiceRedis;
import com.indra.security.model.service.ICacheService;

import io.jsonwebtoken.Jwts;

public class AuthorizationJwtFilter extends BasicAuthenticationFilter 
{

//	@Autowired
//	@Qualifier("cacheService")
	private ICacheService cacheService = new CacheServiceRedis();	

	public AuthorizationJwtFilter(AuthenticationManager authManager) 
	{
		super(authManager);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException
	{
		String header = req.getHeader(HEADER_AUTHORIZACION_KEY);
		if (header == null || !header.startsWith(TOKEN_BEARER_PREFIX)) 
		{
			chain.doFilter(req, res);
			return;
		}
		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request)
	{
		String token = request.getHeader(HEADER_AUTHORIZACION_KEY);
		if (token != null) 
		{
			// Se procesa el token y se recupera el usuario.
			String user = Jwts.parser()
						.setSigningKey(SECRET_KEY)
						.parseClaimsJws(token.replace(TOKEN_BEARER_PREFIX, ""))
						.getBody()
						.getSubject();

			
			if (user != null && cacheService.existsValue(user)) 
			{
				//pendiente revisar GRANTEDs
				return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
			}
			return null;
		}
		return null;
	}
}
