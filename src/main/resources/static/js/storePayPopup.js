//Pay 페이지에서 팝업을 띄울때 사용할 메서드
function payPopup(){
    let itemName = document.getElementById("itemName").innerHTML;
    let itemCount = document.getElementById("num").innerHTML;
    let popUrl = "purchase?itemName=" + itemName + "&itemCount=" + itemCount;
    let popOption = "top=100, left=400, width=1000, height=800, status=no, menubar=no, toolbar=no, resizable=no";
    let pop = window.open(popUrl, "Pay", popOption);
}