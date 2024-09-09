const urlParameters = new URLSearchParams(window.location.search);
const fromParameter = urlParameters.get('from');
const toParameter = urlParameters.get('to');
console.log(toParameter)
const elemFrom = document.getElementsByName('from')[0];
const elemTo = document.getElementsByName('to')[0];
elemFrom.value = fromParameter;
if (elemFrom.value !== "") {
    elemTo.value = Number(fromParameter) + 2000;
}