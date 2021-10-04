<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ include file="/WEB-INF/views/include/bootstrap.jsp" %>
<!DOCTYPE html>
<html>
<head>
	<title>코스모 뱅크</title>
	<meta id="_csrf" name="_csrf" content="${_csrf.token}"/>
 	<meta id="_csrf_header" name="_csrf_header" content="${_csrf.headerName}"/>
	<%@ include file="/WEB-INF/views/include/setting.jsp" %>
</head>
<body>

	<div class="wrapper">
		<jsp:include page="/WEB-INF/views/include/header.jsp" />
		
		<sec:authorize access="hasRole('ROLE_ADMIN')">
			<jsp:include page="/WEB-INF/views/include/mngSidebar.jsp" />
		</sec:authorize>
		<sec:authorize access="hasRole('ROLE_USER')">
			<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />		
		</sec:authorize>
		
		<!-- 메인 폼-->
		<div class="main-panel">
			<div class="content">
				<div class="panel-header bg-primary-gradient" style="height: 300px;">
					<div class="page-inner py-5">
						<div class="d-flex align-items-left align-items-md-center flex-column flex-md-row">
							<div>
								<h1 class="text-white pb-2 fw-bold">KOSMO BANK</h1> <br/>
								<h2 class="text-white op-7 mb-2">KOSMO BANK에 오신 것을 환영합니다.<br/>
									저희는 고객님의 <strong>자산관리</strong>를 효율적이고, 안전하게 도와드립니다. </br>
									또한 <strong>오픈뱅킹</strong> 서비스를 활용하여 보다 편리하게 통합하여 금융상품을 이용하실 수 있습니다.</h2>
							</div>
						</div>
					</div>
				</div>
				
				<div class="notice">
					<!-- 관리자 공지사항 -->
					
					<c:if test="${selectCnt == 0}">
						<script type="text/javascript">
							alert("비밀번호가 틀렸습니다!");
							window.history.back();
						</script>
					</c:if>

					<!-- 비밀번호 인증 성공 -->
					<c:if test="${selectCnt != 0}">
						<form action="mngNoticeModifyAction" method="post" name="modifyform">
						<sec:csrfInput/>
							<!-- input type="hidden"은 form 태그안에 지정해야 한다. -->
							<input type="hidden" name="notice_num" value="${vo.notice_num}"> 
							<input type="hidden" name="pageNum" value="${pageNum}">
				
							<h2 align="center"> 공지사항 수정하기!!</h2>
							
							<table style="width:1000px; margin:auto">
								<tr style="border-bottom: 1px solid #444444; width:500px;">
									<th align="center">
										글제목 : 
									</th>
									<td align="center" colspan="3">
										<input type="text" name="notice_subject" value="${vo.notice_subject}" size="80px"/>
									</td>
								</tr>
								<tr style="border-bottom: 1px solid #444444; width:500px;">
									<th> 작성자 : </th>
									<td>${vo.notice_writer} </td>
									<th> 비밀번호 : </th>
									<td>
										<input type="password" value="${vo.notice_password}" name="notice_password" />
									</td>
								</tr>
								<tr style = "border-bottom: 1px solid #444444; width:500px; text-align:center">
									<th> 글내용 : </th>
									<td colspan="3" style="width:800px">
										<textarea rows="10" cols="50" name="notice_content" word-break:break-all>${vo.notice_content}</textarea>
									</td>
								</tr>
				
								<tr>
									<th colspan="4">
									<input class="button" type="submit" value="글수정">
									<input class="button" type="reset" value="초기화">
									<input class="button" type="submit" value="목록"
										formaction="noticeList.do?pageNum=${pageNum}">
									</th>
								</tr>
							</table>
						</form>
					</c:if>
				</div>
			</div>
		</div>
		<jsp:include page="/WEB-INF/views/include/footer.jsp" />
		</div>
	</div>

	
	<!--   Core JS Files   -->
	<script src="${rePath}js/core/jquery.3.2.1.min.js"></script>
	<script src="${rePath}js/core/popper.min.js"></script>
	<script src="${rePath}js/core/bootstrap.min.js"></script>

	<!-- jQuery UI -->
	<script src="${rePath}js/plugin/jquery-ui-1.12.1.custom/jquery-ui.min.js"></script>
	<script src="${rePath}js/plugin/jquery-ui-touch-punch/jquery.ui.touch-punch.min.js"></script>

	<!-- jQuery Scrollbar -->
	<script src="${rePath}js/plugin/jquery-scrollbar/jquery.scrollbar.min.js"></script>


	<!-- Chart JS -->
	<script src="${rePath}js/plugin/chart.js/chart.min.js"></script>

	<!-- jQuery Sparkline -->
	<script src="${rePath}js/plugin/jquery.sparkline/jquery.sparkline.min.js"></script>

	<!-- Chart Circle -->
	<script src="${rePath}js/plugin/chart-circle/circles.min.js"></script>

	<!-- Datatables -->
	<script src="${rePath}js/plugin/datatables/datatables.min.js"></script>

	<!-- Bootstrap Notify -->
	<script src="${rePath}js/plugin/bootstrap-notify/bootstrap-notify.min.js"></script>

	<!-- jQuery Vector Maps -->
	<script src="${rePath}js/plugin/jqvmap/jquery.vmap.min.js"></script>
	<script src="${rePath}js/plugin/jqvmap/maps/jquery.vmap.world.js"></script>

	<!-- Sweet Alert -->
	<script src="${rePath}js/plugin/sweetalert/sweetalert.min.js"></script>

	<!-- Atlantis JS -->
	<script src="${rePath}js/atlantis.min.js"></script>

	<!-- Atlantis DEMO methods, don't include it in your project! -->
	<script src="${rePath}js/setting-demo.js"></script>
	<script src="${rePath}js/demo.js"></script>
	<script>
		Circles.create({
			id:'circles-1',
			radius:45,
			value:60,
			maxValue:100,
			width:7,
			text: 5,
			colors:['#f1f1f1', '#FF9E27'],
			duration:400,
			wrpClass:'circles-wrp',
			textClass:'circles-text',
			styleWrapper:true,
			styleText:true
		})

		Circles.create({
			id:'circles-2',
			radius:45,
			value:70,
			maxValue:100,
			width:7,
			text: 36,
			colors:['#f1f1f1', '#2BB930'],
			duration:400,
			wrpClass:'circles-wrp',
			textClass:'circles-text',
			styleWrapper:true,
			styleText:true
		})

		Circles.create({
			id:'circles-3',
			radius:45,
			value:40,
			maxValue:100,
			width:7,
			text: 12,
			colors:['#f1f1f1', '#F25961'],
			duration:400,
			wrpClass:'circles-wrp',
			textClass:'circles-text',
			styleWrapper:true,
			styleText:true
		})

		var totalIncomeChart = document.getElementById('totalIncomeChart').getContext('2d');

		var mytotalIncomeChart = new Chart(totalIncomeChart, {
			type: 'bar',
			data: {
				labels: ["S", "M", "T", "W", "T", "F", "S", "S", "M", "T"],
				datasets : [{
					label: "Total Income",
					backgroundColor: '#ff9e27',
					borderColor: 'rgb(23, 125, 255)',
					data: [6, 4, 9, 5, 4, 6, 4, 3, 8, 10],
				}],
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				legend: {
					display: false,
				},
				scales: {
					yAxes: [{
						ticks: {
							display: false //this will remove only the label
						},
						gridLines : {
							drawBorder: false,
							display : false
						}
					}],
					xAxes : [ {
						gridLines : {
							drawBorder: false,
							display : false
						}
					}]
				},
			}
		});

		$('#lineChart').sparkline([105,103,123,100,95,105,115], {
			type: 'line',
			height: '70',
			width: '100%',
			lineWidth: '2',
			lineColor: '#ffa534',
			fillColor: 'rgba(255, 165, 52, .14)'
		});
	</script>
<jsp:include page="/WEB-INF/views/include/chatbot.jsp" />
 
</body>
</html>