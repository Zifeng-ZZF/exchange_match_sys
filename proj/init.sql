
create table accounts (
                          account_num INTEGER PRIMARY KEY,
                          balance real not null
);

create table open_orders (
                             id serial PRIMARY KEY,
                             account_num INTEGER NOT NULL references accounts,
                             symbol varchar(20) NOT NULL,
                             amount real NOT NULL,
                             limit_price real NOT NULL,
                             create_date timestamp NOT NULL
);

create table cancel_orders (
                               id integer,
                               account_num INTEGER NOT NULL references accounts,
                               symbol varchar(20) NOT NULL,
                               amount real NOT NULL,
                               limit_price real NOT NULL,
                               create_date timestamp NOT NULL,
                               canceled_date timestamp NOT NULL
);

create table executed_orders (
                                 id integer,
                                 account_num INTEGER NOT NULL references accounts,
                                 symbol varchar(20) NOT NULL,
                                 amount real NOT NULL,
                                 final_price real NOT NULL,
                                 create_date timestamp NOT NULL,
                                 executed_date timestamp NOT NULL
);

create table account_symbol (
                                account_num Integer references accounts,
                                symbol varchar(20) not null,
                                amount real not null
);
