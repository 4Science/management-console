<%@include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="app-base" >
	<tiles:putAttribute name="title">
		Duradmin: Login
	</tiles:putAttribute>
	
	<tiles:putAttribute name="header-extensions">
		<link rel="stylesheet"  href="${pageContext.request.contextPath}/style/login.css" type="text/css" />
	</tiles:putAttribute>
	
	<tiles:putAttribute name="body">
		<form id="loginForm" action="${pageContext.request.contextPath}/j_spring_security_check" method="post" >
			<div id="login-wrapper">
				<div id="login-header" class="outer clearfix">
					<div id="dc-logo-panel"><a href="/duradmin/spaces" id="dc-logo"></a><span id="dc-app-title"></span></div>			
				</div>
			
				<div id="login-content" class="pane-L1-body clearfix">
					<h1 id="title" class="float-l">Login</h1>
					<div id="form-fields" class="float-r">
						<div id="msg-error" class="error" style="display:none">Username/Password combination not valid. Please try again.</div>
						<ul>
							<li class="clearfix">
								<label for="j_username">Username</label>							
								<input type="text" name="j_username" class="field"/>	
							</li>
							<li class="clearfix">
								<label for="j_password">Password</label>
								<input type="password" name="j_password" class="field"/>
							</li>
							<li class="clearfix">
								<a href="#" id="forgot-password">Forgot Password?</a>
								<a id="button-login" class="flex button float-r" href="javascript:document.forms['loginForm'].submit();"><span>Login</span></a>				
							</li>
						</ul>
					
					</div>
				</div>
				
				<div id="login-footer" class="outer footer clearfix">
					<div class="float-r" id="logo-ds"></div>
					<div class="footer-content">
						Duracloud Administrator Release 0.4  <span class="sep">|</span>
						�<script type="text/javascript">document.write(new Date().getFullYear());</script>
						<a target="_blank" href="http://www.duraspace.org">DuraSpace.org</a>  <span class="sep">|</span>
						<a target="_blank" href="http://www.duracloud.org">Duracloud.org</a>  <span class="sep">|</span> 
						<a target="_blank" href="#">Contact Us</a>
					</div>
				</div>
			</div>
		
			<!-- 
			<div id="old-layout">
				<table class="basic-form" style="max-width:400px">
					<tr>
						<td class="label">
							<label for="j_username">Username</label>
						</td>
						<td class="input">
							<input type="text" name="j_username"/>								
						</td>
					</tr>
					<tr>
						<td class="label">
							<label for="j_password">Password</label>
						</td>
						<td class="input">
							<input type="password" name="j_password"/>
						</td>
					</tr>
				</table>
			</div>
			<div class="basic-form-buttons" >
				<input type="submit" value="Login" />					
			</div>
			-->
		</form>
	</tiles:putAttribute>
</tiles:insertDefinition>