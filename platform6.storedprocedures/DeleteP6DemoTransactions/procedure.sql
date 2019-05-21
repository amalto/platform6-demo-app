delete
from p6core.item
where xpath_exists('/TransactionInfo/KeyValue[./Key = "Application" and Value="p6_demo"]' , content);

delete
from p6core.item
where xpath_exists('/WorkflowTask/flink/view[.="p6_demo.Transactions"]' , content);
