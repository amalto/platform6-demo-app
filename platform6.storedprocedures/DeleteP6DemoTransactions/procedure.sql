delete
from p6core.transaction
where dataType = 'TransactionInfo';

delete
from p6core.transaction
where xpath_exists('/WorkflowTask/flink/view[.="p6_demo.Transactions"]' , content);
