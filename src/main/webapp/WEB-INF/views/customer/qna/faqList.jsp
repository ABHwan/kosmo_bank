<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/setting.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta charset="UTF-8">
<title>자주묻는 질문 게시판</title>
<link rel="stylesheet" href="${rePath}css/manager/admin1.css" />
<!-- CSS Files -->
<link rel="stylesheet" href="${rePath}css/user/qnaList.css">
<link rel="stylesheet" href="${rePath}css/bootstrap.min.css">
<link rel="stylesheet" href="${rePath}css/atlantis.min.css">
<!-- CSS Just for demo purpose, don't include it in your project -->
<link rel="stylesheet" href="${rePath}css/demo.css">
<!-- Fonts and icons -->
<script src="${rePath}js/plugin/webfont/webfont.min.js"></script>
<script>
	WebFont.load({
		google : {
			"families" : [ "Lato:300,400,700,900" ]
		},
		custom : {
			"families" : [ "Flaticon", "Font Awesome 5 Solid",
					"Font Awesome 5 Regular", "Font Awesome 5 Brands",
					"simple-line-icons" ],
			urls : [ '${rePath}css/fonts.min.css' ]
		},
		active : function() {
			sessionStorage.fonts = true;
		}
	});
</script>
</head>
<body>
<div class="wrapper">
		<jsp:include page="/WEB-INF/views/include/header.jsp" />
		<jsp:include page="/WEB-INF/views/include/sidebar.jsp" />
		<!-- 메인 폼-->
		<div class="main-panel">
			<div class="content">
			<!-- 고정헤더 -->
				<div class="panel-header bg-primary-gradient" style="height: 300px;">
					<div class="page-inner">
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
				<section id="main" >
			      <div class="main__container" style="width: 95%;">
					<h4 class="page-title">자주하는 질문(FAQ)</h4>
					<div class="row">
						<div class="col">
							<div class="card">
								<div class="card-header">
									<div class="card-title">FAQ</div>
								</div>
								<div class="card-body">
									<form action="faqAdd" method="post">
									<sec:csrfInput />
									<input type="hidden" name="pageNum"value="${pageNum }"><!-- pageNum -->
									<c:if test="${cnt > 0 }">
										<c:forEach var="dto" items="${dtos}">
										<details>
							<summary>
						<table class="display table table-striped table-hover dataTable">
					        <tr class="hide">
					        	<!-- 문의 번호 -->
					        	<th width="30px">Q
					        		<c:set var="number" value="${number - 1}" />
					        		<!-- qna_seq 번호 -->
					        		<input type="hidden" name="faq_id" value="${dto.faq_id}">
					        		<input type="hidden" name="faq_regDate" value="${dto.faq_regDate}">
					        	</th>
					            <!-- 문의 제목 [문의유형] 제목 -->
					            <td id="faq_title" align="left">${dto.faq_title}</td>
					            <!-- 문의 등록일 -->
								
					         </tr>
				         </table>
				         
				         </summary>
					        <table  class="display table table-striped table-hover dataTable">
					         <tr>
					         	<c:if test="${dto.faq_content == null }" >
					         		<th width="30px">
						         	<div><i class="icon-pencil"></i></div>
						         	</th>
						         	<td text-align="center" colspan="4">
						         		답변이 등록되지 않았습니다.
						         	</td>
						         	
					         	</c:if>
					         	<c:if test="${dto.faq_content != null }" >
					         		<th width="30px">
						         	<i class="icon-pencil"></i>
						         	</th>
					         		<td text-align="center" colspan="4">
					         			${dto.faq_content}
					         		</td>
					         		
					         	</c:if>
					         </tr>
				         </table> 
				         </details>
							</c:forEach>
							</c:if>
										
								<c:if test="${cnt == 0}">
								
									<tr align="center">
										<th scope="col" colspan="4">FAQ가 등록되지 않았습니다.</th>
									</tr>
								</c:if>
								
									</form>
								</div>
								  <div class="pagenation">
			        		 <ul>
			          	   <!-- paging -->
                           <ul class="pagination pg-primary mt-5">
                           <c:if test="${cnt > 0}">
                              <!-- 이전블록[«] -->
                              <c:if test="${startPage > pageBlock}">
                                 <li class="page-item">
                                    <a class="page-link" href="loanProductList?pageNum=${startPage - pageBlock}" aria-label="Previous">
                                       <span aria-hidden="true">«</span>
                                       <span class="sr-only">Previous</span>
                                    </a>
                                 </li>   
                              </c:if>
                              
                              <!-- 블록 내의 페이지 번호 -->
                              <c:forEach var="i" begin="${startPage}" end="${endPage}" >
                                 <c:if test="${i == currentPage}">
                                    <li class="page-item active">
                                       <a class="page-link" href="loanProductList?pageNum=${i}">${i}</a>
                                    </li>
                                 </c:if>
                                 
                                 
                                 <c:if test="${i != currentPage}">
                                    <li class="page-item">
                                       <a class="page-link" href="loanProductList?pageNum=${i}">${i}</a>
                                    </li>
                                 </c:if>
                              </c:forEach>
                              
                              <!-- 다음블록[»] -->
                              <c:if test="${pageCount > endPage}" >
                                 <li class="page-item">
                                    <a class="page-link" href="loanProductList?pageNum=${startPage + pageBlock}" aria-label="Next">
                                       <span aria-hidden="true">»</span>
                                       <span class="sr-only">Next</span>
                                    </a>
                                 </li>
                              </c:if>
                           </c:if>
                           </ul>
                           <!-- paging -->
                           </ul>
							</div>
						</div>
					</div>
					
					</div>
				</div>
				</section>
			</div>
			
			
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
</body>
</html>