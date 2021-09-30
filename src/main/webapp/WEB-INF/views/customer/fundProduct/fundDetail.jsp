 <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/setting.jsp" %>
<%@ include file="/WEB-INF/views/include/bootstrap.jsp" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>펀드 상세 보기</title>
<!-- CSS -->
<link rel="stylesheet" href="${rePath}css/manager/admin1.css" />


<script>
	var msg = "<%=request.getAttribute("msg") %>";
	if(msg != 'null'){
		 alert(msg);
	}
</script>
</head>
<body>

	<!-- <div class="wrapper">
		<div class="main-header"> -->
		<jsp:include page="/WEB-INF/views/include/header.jsp" />
		<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />
		<!-- 메인 폼-->
		<div class="main-panel">
			<div class="content">
				<!-- 고정헤더 -->
				<div class="panel-header bg-primary-gradient" style="height: 300px;">
					<div class="page-inner py-5">
						<div class="d-flex align-items-left align-items-md-center flex-column flex-md-row">
							<div>
								<h1 class="text-white pb-2 fw-bold">KOSMO BANK</h1> <br/>
								<h2 class="text-white op-7 mb-2">KOSMO BANK에 오신 것을 환영합니다.<br/>
									저희는 고객님의 <strong>자산관리</strong>를 효율적이고, 안전하게 도와드립니다. <br />
									또한 <strong>오픈뱅킹</strong> 서비스를 활용하여 보다 편리하게 통합하여 금융상품을 이용하실 수 있습니다.</h2>
							</div>
						</div>
					</div>
				</div>
				<section id="main">
			      <div class="main__container">
					<h2 class="title">펀드상품 상세</h2>
					<div class="row">
						<div class="col">
							<div class="card">
								<div class="card-header">
									<div class="card-title">${dto.fund_title}</div>
								</div>
								<div class="card-body">
									 <form action="fundProductInsert" name="fundProductForm">
									 	<sec:csrfInput/>
								         <input type="hidden" name="pageNum" value="${pageNum}">
								         <input type="hidden" name="number" value="${number}">
						        <table class="admin__table">
						          <tr>
						            <th class="table__head">펀드상품 한줄요약</th>
						             <td>${dto.fund_summary} 
						             <input type="hidden" name="fund_summary" class="user_check" value="${dto.fund_summary}">
						             </td>
						          </tr>
						          <tr>
						            <td>${dto.fund_filename}
						            <input type="hidden" name="fund_filename" class="user_check" value="${dto.fund_filename}">
						            </td>
						          </tr>
						          <tr>
						            <th class="table__head">펀딩 기간</th>
						              <td><fmt:formatDate value="${dto.fund_start_date}" type="date" /> ~ <fmt:formatDate value="${dto.fund_end_date}" type="date"/>
						              <input type="hidden" name="fund_start_date" class="user_check" value="${dto.fund_start_date}">
						              <input type="hidden" name="fund_end_date" class="user_check" value="${dto.fund_end_date}">
						              </td>
						          </tr>
						          <tr>
						          	 <th class="table__head">달성도</th>
						          	 <td>
						          	 </td>
						           </tr>
						           <tr>
							           <th class="table__head">목표 금액</th>
							           <td><fmt:formatNumber value="${dto.fund_goal_money}" type="number"/>원
							           <input type="hidden" name="fund_product_minDate" class="user_check" value="${dto.fund_goal_money}">
							           </td>
						           </tr>
						           
						           <tr>
								<th>입금 계좌</th>
								<td>
									<c:choose>
				           			<c:when test="${vo.fund_bank_code==0}">
				           				미기재
				           			</c:when>
				           			<c:when test="${vo.fund_bank_code==1}">
				           				국민은행
				           			</c:when>
				           			<c:when test="${vo.fund_bank_code==2}">
				           				우리은행
				           			</c:when>
				           			<c:when test="${vo.fund_bank_code==3}">
				           				농협은행
				           			</c:when>
				           			<c:when test="${vo.fund_bank_code==4}">
				           				신한은행
				           			</c:when>
				           			<c:when test="${vo.fund_bank_code==5}">
				           				하나은행
				           			</c:when>
				           			<c:when test="${vo.fund_bank_code==6}">
				           				코스모은행
				           			</c:when>
				           			</c:choose>
								</td>
								<td>${vo.fund_account}</td>
							</tr>
						           
						    <tr class="form__row">
								<td>${vo.fund_content}</td>
							</tr>
							
							<tr class="form__row">
								<th>등록자 이름</th>
								<td>
									${vo.fund_mem_name}
								</td>
							</tr>
							
							<tr class="form__row">
								<th>등록자 연락처</th>
								<td>
									${vo.fund_mem_hp}
								</td>
							</tr>
							
							<tr class="form__row">
								<th>등록자 이메일</th>
								<td>${vo.fund_mem_email}</td>
							</tr>
						        </table>
						        <div align ="right">
						          	<input type="submit" class="btn btn-primary btn-border" value="펀드상품 신청하기" >
						        </div>
			        </form>
								</div>
							</div>
						</div>
					</div>
				</div>
				</section>
			</div>
		</div>
	
	<script src="${rePath}js/core/jquery.3.2.1.min.js"></script>
	<script src="${rePath}js/core/popper.min.js"></script>
	<script src="${rePath}js/core/bootstrap.min.js"></script>

	<!-- jQuery UI -->
	<script
		src="${rePath}js/plugin/jquery-ui-1.12.1.custom/jquery-ui.min.js"></script>
	<script
		src="${rePath}js/plugin/jquery-ui-touch-punch/jquery.ui.touch-punch.min.js"></script>

	<!-- jQuery Scrollbar -->
	<script
		src="${rePath}js/plugin/jquery-scrollbar/jquery.scrollbar.min.js"></script>


	<!-- Chart JS -->
	<script src="${rePath}js/plugin/chart.js/chart.min.js"></script>

	<!-- jQuery Sparkline -->
	<script
		src="${rePath}js/plugin/jquery.sparkline/jquery.sparkline.min.js"></script>

	<!-- Chart Circle -->
	<script src="${rePath}js/plugin/chart-circle/circles.min.js"></script>

	<!-- Datatables -->
	<script src="${rePath}js/plugin/datatables/datatables.min.js"></script>

	<!-- Bootstrap Notify -->
	<script
		src="${rePath}js/plugin/bootstrap-notify/bootstrap-notify.min.js"></script>

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
			id : 'circles-1',
			radius : 45,
			value : 60,
			maxValue : 100,
			width : 7,
			text : 5,
			colors : [ '#f1f1f1', '#FF9E27' ],
			duration : 400,
			wrpClass : 'circles-wrp',
			textClass : 'circles-text',
			styleWrapper : true,
			styleText : true
		})

		Circles.create({
			id : 'circles-2',
			radius : 45,
			value : 70,
			maxValue : 100,
			width : 7,
			text : 36,
			colors : [ '#f1f1f1', '#2BB930' ],
			duration : 400,
			wrpClass : 'circles-wrp',
			textClass : 'circles-text',
			styleWrapper : true,
			styleText : true
		})

		Circles.create({
			id : 'circles-3',
			radius : 45,
			value : 40,
			maxValue : 100,
			width : 7,
			text : 12,
			colors : [ '#f1f1f1', '#F25961' ],
			duration : 400,
			wrpClass : 'circles-wrp',
			textClass : 'circles-text',
			styleWrapper : true,
			styleText : true
		})

		var totalIncomeChart = document.getElementById('totalIncomeChart')
				.getContext('2d');

		var mytotalIncomeChart = new Chart(totalIncomeChart, {
			type : 'bar',
			data : {
				labels : [ "S", "M", "T", "W", "T", "F", "S", "S", "M", "T" ],
				datasets : [ {
					label : "Total Income",
					backgroundColor : '#ff9e27',
					borderColor : 'rgb(23, 125, 255)',
					data : [ 6, 4, 9, 5, 4, 6, 4, 3, 8, 10 ],
				} ],
			},
			options : {
				responsive : true,
				maintainAspectRatio : false,
				legend : {
					display : false,
				},
				scales : {
					yAxes : [ {
						ticks : {
							display : false
						//this will remove only the label
						},
						gridLines : {
							drawBorder : false,
							display : false
						}
					} ],
					xAxes : [ {
						gridLines : {
							drawBorder : false,
							display : false
						}
					} ]
				},
			}
		});

		$('#lineChart').sparkline([ 105, 103, 123, 100, 95, 105, 115 ], {
			type : 'line',
			height : '70',
			width : '100%',
			lineWidth : '2',
			lineColor : '#ffa534',
			fillColor : 'rgba(255, 165, 52, .14)'
		});
	</script>
</html>