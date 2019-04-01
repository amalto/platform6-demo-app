delete
from B2HEAD.item
where xpath_exists('/TransactionInfo/KeyValue[./Key = "Application" and Value="p6_demo"]' , content);

delete
from B2HEAD.item
where xpath_exists('/WFWorkItem/flink/view[.="p6_demo.Transactions"]' , content);
